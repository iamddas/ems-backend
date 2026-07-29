package com.ems.employee;

import com.ems.employee.dto.EmployeeRequest;
import com.ems.employee.dto.EmployeeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EmployeeService {
    EmployeeResponse create(EmployeeRequest request);
    EmployeeResponse getById(Long id);
    List<EmployeeResponse> getAll();
    Page<EmployeeResponse> getAll(Pageable pageable);
    List<EmployeeResponse> getByDepartment(Long departmentId);
    Page<EmployeeResponse> getByDepartment(Long departmentId, Pageable pageable);
    EmployeeResponse update(Long id, EmployeeRequest request);
    void delete(Long id);
}
