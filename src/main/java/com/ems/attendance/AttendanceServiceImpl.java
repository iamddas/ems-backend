package com.ems.attendance;

import com.ems.attendance.dto.AttendanceRequest;
import com.ems.attendance.dto.AttendanceResponse;
import com.ems.common.exception.ResourceNotFoundException;
import com.ems.employee.Employee;
import com.ems.employee.EmployeeRepository;
import com.ems.model.Role;
import com.ems.model.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    @Transactional
    public AttendanceResponse create(AttendanceRequest request) {
        if (attendanceRepository.existsByEmployeeIdAndDate(request.getEmployeeId(), request.getDate())) {
            throw new IllegalArgumentException(
                    "Attendance already recorded for employee " + request.getEmployeeId()
                    + " on " + request.getDate());
        }
        Employee employee = findEmployeeOrThrow(request.getEmployeeId());
        UserInfo currentUser = getCurrentUser();
        enforceSelfOrAdmin(employee, currentUser);

        AttendanceRecord record = AttendanceRecord.builder()
                .employee(employee)
                .date(request.getDate())
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .status(request.getStatus() != null ? request.getStatus() : AttendanceStatus.PRESENT)
                .notes(request.getNotes())
                .user(currentUser)
                .build();
        return toResponse(attendanceRepository.save(record));
    }

    private static UserInfo getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return principal instanceof UserInfo userInfo ? userInfo : null;
    }

    /** EMPLOYEE-role users may only record/edit their own attendance; ADMIN/SUPER_ADMIN can act on anyone. */
    private static void enforceSelfOrAdmin(Employee employee, UserInfo currentUser) {
        if (currentUser == null || currentUser.getRole() != Role.EMPLOYEE) {
            return;
        }
        boolean isSelf = employee.getUser() != null && employee.getUser().getId().equals(currentUser.getId());
        if (!isSelf) {
            throw new AccessDeniedException("You can only record your own attendance");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AttendanceResponse getById(Long id) {
        AttendanceRecord record = findOrThrow(id);
        enforceSelfOrAdmin(record.getEmployee(), getCurrentUser());
        return toResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getAll() {
        return attendanceRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getByEmployee(Long employeeId) {
        enforceSelfOrAdmin(findEmployeeOrThrow(employeeId), getCurrentUser());
        return attendanceRepository.findByEmployeeId(employeeId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getByEmployeeAndDateRange(Long employeeId, LocalDate from, LocalDate to) {
        enforceSelfOrAdmin(findEmployeeOrThrow(employeeId), getCurrentUser());
        return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, from, to)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public AttendanceResponse update(Long id, AttendanceRequest request) {
        AttendanceRecord record = findOrThrow(id);
        enforceSelfOrAdmin(record.getEmployee(), getCurrentUser());
        record.setCheckIn(request.getCheckIn());
        record.setCheckOut(request.getCheckOut());
        if (request.getStatus() != null) {
            record.setStatus(request.getStatus());
        }
        record.setNotes(request.getNotes());
        return toResponse(attendanceRepository.save(record));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!attendanceRepository.existsById(id)) {
            throw new ResourceNotFoundException("AttendanceRecord", "id", id);
        }
        attendanceRepository.deleteById(id);
    }

    private AttendanceRecord findOrThrow(Long id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AttendanceRecord", "id", id));
    }

    private Employee findEmployeeOrThrow(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", "id", employeeId));
    }

    private AttendanceResponse toResponse(AttendanceRecord record) {
        return AttendanceResponse.builder()
                .id(record.getId())
                .employeeId(record.getEmployee().getId())
                .employeeCode(record.getEmployee().getEmployeeCode())
                .employeeName(record.getEmployee().getFirstName() + " " + record.getEmployee().getLastName())
                .date(record.getDate())
                .checkIn(record.getCheckIn())
                .checkOut(record.getCheckOut())
                .status(record.getStatus())
                .notes(record.getNotes())
                .userId(record.getUser() != null ? record.getUser().getId() : null)
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}
