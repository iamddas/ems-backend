package com.ems.employee.dto;

import com.ems.employee.EmployeeStatus;
import com.ems.model.Role;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class EmployeeResponse {
    private Long id;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String position;
    private BigDecimal salary;
    private LocalDate hireDate;
    private EmployeeStatus status;
    private Long departmentId;
    private String departmentName;
    private Long userId;
    private Role role;
    private String temporaryPassword;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
