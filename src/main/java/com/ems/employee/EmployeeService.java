package com.ems.employee;

import com.ems.employee.dto.EmployeeRequest;
import com.ems.employee.dto.EmployeeResponse;

import java.util.List;

public interface EmployeeService {
    EmployeeResponse create(EmployeeRequest request);
    EmployeeResponse getById(Long id);
    List<EmployeeResponse> getAll();
    List<EmployeeResponse> getByDepartment(Long departmentId);
    EmployeeResponse update(Long id, EmployeeRequest request);
    void delete(Long id);
}
