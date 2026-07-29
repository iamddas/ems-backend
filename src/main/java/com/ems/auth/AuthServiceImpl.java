package com.ems.auth;

import com.ems.auth.dto.AuthResponse;
import com.ems.auth.dto.LoginRequest;
import com.ems.auth.dto.RegisterRequest;
import com.ems.common.exception.ResourceNotFoundException;
import com.ems.employee.Employee;
import com.ems.employee.EmployeeRepository;
import com.ems.model.Role;
import com.ems.model.UserInfo;
import com.ems.repository.UserInfoRepository;
import com.ems.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserInfoRepository userInfoRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userInfoRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + request.getEmail());
        }

        UserInfo user = UserInfo.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.EMPLOYEE)
                .active(true)
                .build();

        userInfoRepository.save(user);

        if (user.getRole() != Role.ADMIN && user.getRole() != Role.SUPER_ADMIN) {
            Employee employee = new Employee();
            employee.setEmployeeCode(generateEmployeeCode(user.getId()));
            employee.setFirstName(user.getFirstName());
            employee.setLastName(user.getLastName());
            employee.setEmail(user.getEmail());
            employee.setPosition("Not Assigned");
            employee.setUser(user);
            employeeRepository.save(employee);
        }

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .id(user.getId())
                .token(token)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .avatar(user.getProfilePictureUrl())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        UserInfo user = userInfoRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        String token = jwtService.generateToken(user);

        return AuthResponse.builder()
                .id(user.getId())
                .token(token)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole().name())
                .avatar(user.getProfilePictureUrl())
                .build();
    }

    private String generateEmployeeCode(Long userId) {
        String code = "EMP" + String.format("%04d", userId);
        while (employeeRepository.existsByEmployeeCode(code)) {
            code = code + "X";
        }
        return code;
    }
}
