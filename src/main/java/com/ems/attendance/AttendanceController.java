package com.ems.attendance;

import com.ems.attendance.dto.AttendanceRequest;
import com.ems.attendance.dto.AttendanceResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping
    public ResponseEntity<AttendanceResponse> create(@Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(attendanceService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AttendanceResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getById(id));
    }

    // Dual-mode: no page/size -> full list. page and/or size present -> paginated response.
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page == null && size == null) {
            return ResponseEntity.ok(attendanceService.getAll());
        }
        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 20, Sort.by(Sort.Direction.DESC, "date"));
        return ResponseEntity.ok(attendanceService.getAll(pageable));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<?> getByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page == null && size == null) {
            return ResponseEntity.ok(attendanceService.getByEmployee(employeeId));
        }
        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 20, Sort.by(Sort.Direction.DESC, "date"));
        return ResponseEntity.ok(attendanceService.getByEmployee(employeeId, pageable));
    }

    @GetMapping("/employee/{employeeId}/range")
    public ResponseEntity<?> getByRange(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page == null && size == null) {
            return ResponseEntity.ok(attendanceService.getByEmployeeAndDateRange(employeeId, from, to));
        }
        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 20, Sort.by(Sort.Direction.DESC, "date"));
        return ResponseEntity.ok(attendanceService.getByEmployeeAndDateRange(employeeId, from, to, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AttendanceResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.ok(attendanceService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        attendanceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
