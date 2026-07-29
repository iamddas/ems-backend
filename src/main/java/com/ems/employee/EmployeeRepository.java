package com.ems.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    Optional<Employee> findByEmployeeCode(String employeeCode);

    @EntityGraph(attributePaths = {"department", "user"})
    @Override
    List<Employee> findAll();

    @EntityGraph(attributePaths = {"department", "user"})
    @Override
    Page<Employee> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"department", "user"})
    @Override
    Optional<Employee> findById(Long id);

    @EntityGraph(attributePaths = {"department", "user"})
    List<Employee> findByDepartmentId(Long departmentId);

    @EntityGraph(attributePaths = {"department", "user"})
    Page<Employee> findByDepartmentId(Long departmentId, Pageable pageable);

    List<Employee> findByStatus(EmployeeStatus status);
    boolean existsByEmail(String email);
    boolean existsByEmployeeCode(String employeeCode);
}
