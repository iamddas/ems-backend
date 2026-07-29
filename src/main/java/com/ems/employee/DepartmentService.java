package com.ems.employee;

import com.ems.employee.dto.DepartmentRequest;
import com.ems.employee.dto.DepartmentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DepartmentService {
    DepartmentResponse create(DepartmentRequest request);
    DepartmentResponse getById(Long id);
    List<DepartmentResponse> getAll();
    Page<DepartmentResponse> getAll(Pageable pageable);
    DepartmentResponse update(Long id, DepartmentRequest request);
    void delete(Long id);
}
