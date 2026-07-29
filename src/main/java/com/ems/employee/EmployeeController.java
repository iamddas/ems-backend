package com.ems.employee;

import com.ems.employee.dto.EmployeeRequest;
import com.ems.employee.dto.EmployeeResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    // Dual-mode: no page/size -> full list (chat contacts, punch self-lookup, dashboard rely on this).
    // page and/or size present -> paginated response, used by the Employees directory table.
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page == null && size == null) {
            return ResponseEntity.ok(employeeService.getAll());
        }
        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 20);
        return ResponseEntity.ok(employeeService.getAll(pageable));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<?> getByDepartment(
            @PathVariable Long departmentId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page == null && size == null) {
            return ResponseEntity.ok(employeeService.getByDepartment(departmentId));
        }
        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 20);
        return ResponseEntity.ok(employeeService.getByDepartment(departmentId, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<EmployeeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {
        return ResponseEntity.ok(employeeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
