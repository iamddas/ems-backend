package com.ems.employee;

import com.ems.common.exception.ResourceNotFoundException;
import com.ems.employee.dto.EmployeeRequest;
import com.ems.employee.dto.EmployeeResponse;
import com.ems.model.Role;
import com.ems.model.UserInfo;
import com.ems.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private static final String TEMP_PASSWORD_CHARS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserInfoRepository userInfoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {
        if (employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }
        if (employeeRepository.existsByEmployeeCode(request.getEmployeeCode())) {
            throw new IllegalArgumentException("Employee code already in use: " + request.getEmployeeCode());
        }

        Employee employee = new Employee();
        applyRequest(employee, request);

        String temporaryPassword = null;
        UserInfo existingUser = userInfoRepository.findByEmail(request.getEmail()).orElse(null);
        if (existingUser != null) {
            employee.setUser(existingUser);
        } else {
            temporaryPassword = generateTempPassword();
            UserInfo newUser = UserInfo.builder()
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .email(request.getEmail())
                    .password(passwordEncoder.encode(temporaryPassword))
                    .role(request.getRole() != null ? request.getRole() : Role.EMPLOYEE)
                    .active(true)
                    .build();
            employee.setUser(userInfoRepository.save(newUser));
        }

        syncLoginAccessWithStatus(employee);

        EmployeeResponse response = toResponse(employeeRepository.save(employee));
        response.setTemporaryPassword(temporaryPassword);
        return response;
    }

    private static String generateTempPassword() {
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(TEMP_PASSWORD_CHARS.charAt(RANDOM.nextInt(TEMP_PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getAll() {
        return employeeRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getAll(Pageable pageable) {
        return employeeRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> getByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeResponse> getByDepartment(Long departmentId, Pageable pageable) {
        return employeeRepository.findByDepartmentId(departmentId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public EmployeeResponse update(Long id, EmployeeRequest request) {
        Employee employee = findOrThrow(id);
        applyRequest(employee, request);

        boolean wantsRoleChange = request.getRole() != null;
        boolean wantsPasswordChange = request.getNewPassword() != null && !request.getNewPassword().isBlank();
        if (wantsRoleChange || wantsPasswordChange) {
            if (!isSuperAdmin(getCurrentUser())) {
                throw new AccessDeniedException("Only Super Admin can change an employee's role or password");
            }
            UserInfo linkedUser = employee.getUser();
            if (linkedUser != null) {
                if (wantsRoleChange) {
                    linkedUser.setRole(request.getRole());
                }
                if (wantsPasswordChange) {
                    linkedUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
                }
                userInfoRepository.save(linkedUser);
            }
        }

        syncLoginAccessWithStatus(employee);

        return toResponse(employeeRepository.save(employee));
    }

    // An INACTIVE employee's linked login account is disabled — Spring Security's
    // DaoAuthenticationProvider checks UserInfo.isEnabled() and rejects login with
    // DisabledException before password verification even runs.
    private void syncLoginAccessWithStatus(Employee employee) {
        UserInfo linkedUser = employee.getUser();
        if (linkedUser == null) {
            return;
        }
        boolean shouldBeActive = employee.getStatus() != EmployeeStatus.INACTIVE;
        if (shouldBeActive != Boolean.TRUE.equals(linkedUser.getActive())) {
            linkedUser.setActive(shouldBeActive);
            userInfoRepository.save(linkedUser);
        }
    }

    private static UserInfo getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof UserInfo userInfo ? userInfo : null;
    }

    private static boolean isSuperAdmin(UserInfo user) {
        return user != null && user.getRole() == Role.SUPER_ADMIN;
    }

    private static boolean isAdminOrSuperAdmin(UserInfo user) {
        return user != null && (user.getRole() == Role.ADMIN || user.getRole() == Role.SUPER_ADMIN);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee", "id", id);
        }
        employeeRepository.deleteById(id);
    }

    private void applyRequest(Employee employee, EmployeeRequest request) {
        employee.setEmployeeCode(request.getEmployeeCode());
        employee.setFirstName(request.getFirstName());
        employee.setLastName(request.getLastName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setPosition(request.getPosition());
        employee.setSalary(request.getSalary());
        employee.setHireDate(request.getHireDate());
        if (request.getStatus() != null) {
            employee.setStatus(request.getStatus());
        }
        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", "id", request.getDepartmentId()));
            employee.setDepartment(dept);
        }
    }

    private Employee findOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", id));
    }

    private EmployeeResponse toResponse(Employee emp) {
        boolean canSeeSalary = isAdminOrSuperAdmin(getCurrentUser());
        return EmployeeResponse.builder()
                .id(emp.getId())
                .employeeCode(emp.getEmployeeCode())
                .firstName(emp.getFirstName())
                .lastName(emp.getLastName())
                .email(emp.getEmail())
                .phone(emp.getPhone())
                .position(emp.getPosition())
                .salary(canSeeSalary ? emp.getSalary() : null)
                .hireDate(emp.getHireDate())
                .status(emp.getStatus())
                .departmentId(emp.getDepartment() != null ? emp.getDepartment().getId() : null)
                .departmentName(emp.getDepartment() != null ? emp.getDepartment().getName() : null)
                .userId(emp.getUser() != null ? emp.getUser().getId() : null)
                .role(emp.getUser() != null ? emp.getUser().getRole() : null)
                .createdAt(emp.getCreatedAt())
                .updatedAt(emp.getUpdatedAt())
                .build();
    }
}
