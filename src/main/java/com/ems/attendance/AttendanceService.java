package com.ems.attendance;

import com.ems.attendance.dto.AttendanceRequest;
import com.ems.attendance.dto.AttendanceResponse;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    AttendanceResponse create(AttendanceRequest request);
    AttendanceResponse getById(Long id);
    List<AttendanceResponse> getAll();
    List<AttendanceResponse> getByEmployee(Long employeeId);
    List<AttendanceResponse> getByEmployeeAndDateRange(Long employeeId, LocalDate from, LocalDate to);
    AttendanceResponse update(Long id, AttendanceRequest request);
    void delete(Long id);
}
