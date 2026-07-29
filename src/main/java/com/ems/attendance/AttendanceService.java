package com.ems.attendance;

import com.ems.attendance.dto.AttendanceRequest;
import com.ems.attendance.dto.AttendanceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    AttendanceResponse create(AttendanceRequest request);
    AttendanceResponse getById(Long id);
    List<AttendanceResponse> getAll();
    Page<AttendanceResponse> getAll(Pageable pageable);
    List<AttendanceResponse> getByEmployee(Long employeeId);
    Page<AttendanceResponse> getByEmployee(Long employeeId, Pageable pageable);
    List<AttendanceResponse> getByEmployeeAndDateRange(Long employeeId, LocalDate from, LocalDate to);
    Page<AttendanceResponse> getByEmployeeAndDateRange(Long employeeId, LocalDate from, LocalDate to, Pageable pageable);
    AttendanceResponse update(Long id, AttendanceRequest request);
    void delete(Long id);
}
