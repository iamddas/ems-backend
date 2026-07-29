package com.ems.employee;

import com.ems.employee.dto.DepartmentRequest;
import com.ems.employee.dto.DepartmentResponse;
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
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getById(id));
    }

    // Dual-mode: no page/size -> full list (dropdowns, chat, etc. rely on this).
    // page and/or size present -> paginated response, used by the Departments table.
    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        if (page == null && size == null) {
            return ResponseEntity.ok(departmentService.getAll());
        }
        Pageable pageable = PageRequest.of(page != null ? page : 0, size != null ? size : 20);
        return ResponseEntity.ok(departmentService.getAll(pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<DepartmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {
        return ResponseEntity.ok(departmentService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
