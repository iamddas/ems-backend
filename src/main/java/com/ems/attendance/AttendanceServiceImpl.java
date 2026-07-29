package com.ems.attendance;

import com.ems.attendance.dto.AttendanceRequest;
import com.ems.attendance.dto.AttendanceResponse;
import com.ems.common.exception.ResourceNotFoundException;
import com.ems.employee.Employee;
import com.ems.employee.EmployeeRepository;
import com.ems.model.Role;
import com.ems.model.UserInfo;
import com.ems.notification.NotificationService;
import com.ems.notification.NotificationType;
import com.ems.notification.dto.NotificationRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final NotificationService notificationService;

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
        AttendanceRecord saved = attendanceRepository.save(record);

        if (saved.getCheckIn() != null) {
            notifySelf(employee.getUser(), "Punched In", NotificationType.SUCCESS,
                    "You checked in at " + saved.getCheckIn() + " on " + saved.getDate());
        }

        return toResponse(saved);
    }

    private void notifySelf(UserInfo recipient, String title, NotificationType type, String message) {
        if (recipient == null) {
            return;
        }
        NotificationRequest notification = new NotificationRequest();
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRecipientId(recipient.getId());
        notificationService.create(notification);
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
    public Page<AttendanceResponse> getAll(Pageable pageable) {
        return attendanceRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getByEmployee(Long employeeId) {
        enforceSelfOrAdmin(findEmployeeOrThrow(employeeId), getCurrentUser());
        return attendanceRepository.findByEmployeeId(employeeId).stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getByEmployee(Long employeeId, Pageable pageable) {
        enforceSelfOrAdmin(findEmployeeOrThrow(employeeId), getCurrentUser());
        return attendanceRepository.findByEmployeeId(employeeId, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttendanceResponse> getByEmployeeAndDateRange(Long employeeId, LocalDate from, LocalDate to) {
        enforceSelfOrAdmin(findEmployeeOrThrow(employeeId), getCurrentUser());
        return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, from, to)
                .stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getByEmployeeAndDateRange(Long employeeId, LocalDate from, LocalDate to, Pageable pageable) {
        enforceSelfOrAdmin(findEmployeeOrThrow(employeeId), getCurrentUser());
        return attendanceRepository.findByEmployeeIdAndDateBetween(employeeId, from, to, pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public AttendanceResponse update(Long id, AttendanceRequest request) {
        AttendanceRecord record = findOrThrow(id);
        enforceSelfOrAdmin(record.getEmployee(), getCurrentUser());
        boolean justCheckedOut = record.getCheckOut() == null && request.getCheckOut() != null;
        record.setCheckIn(request.getCheckIn());
        record.setCheckOut(request.getCheckOut());
        if (request.getStatus() != null) {
            record.setStatus(request.getStatus());
        }
        record.setNotes(request.getNotes());
        AttendanceRecord saved = attendanceRepository.save(record);

        if (justCheckedOut) {
            notifySelf(saved.getEmployee().getUser(), "Punched Out", NotificationType.SUCCESS,
                    "You checked out at " + saved.getCheckOut() + " on " + saved.getDate());
        }

        return toResponse(saved);
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
