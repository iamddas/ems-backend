package com.ems.employee.dto;

import com.ems.employee.EmployeeStatus;
import com.ems.model.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class EmployeeRequest {

    @NotBlank
    private String employeeCode;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    @NotBlank
    private String email;

    private String phone;

    @NotBlank
    private String position;

    @Positive
    private BigDecimal salary;

    private LocalDate hireDate;

    private EmployeeStatus status;

    private Long departmentId;

    private Role role;
}
