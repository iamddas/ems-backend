package com.ems.employee;

import com.ems.employee.dto.DepartmentRequest;
import com.ems.employee.dto.DepartmentResponse;

import java.util.List;

public interface DepartmentService {
    DepartmentResponse create(DepartmentRequest request);
    DepartmentResponse getById(Long id);
    List<DepartmentResponse> getAll();
    DepartmentResponse update(Long id, DepartmentRequest request);
    void delete(Long id);
}
