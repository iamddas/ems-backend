package com.ems.attendance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<AttendanceRecord, Long> {
    @EntityGraph(attributePaths = {"employee", "user"})
    @Override
    List<AttendanceRecord> findAll();

    @EntityGraph(attributePaths = {"employee", "user"})
    @Override
    Page<AttendanceRecord> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"employee", "user"})
    List<AttendanceRecord> findByEmployeeId(Long employeeId);

    @EntityGraph(attributePaths = {"employee", "user"})
    Page<AttendanceRecord> findByEmployeeId(Long employeeId, Pageable pageable);

    @EntityGraph(attributePaths = {"employee", "employee.user"})
    List<AttendanceRecord> findByDate(LocalDate date);

    @EntityGraph(attributePaths = {"employee", "user"})
    List<AttendanceRecord> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate from, LocalDate to);

    @EntityGraph(attributePaths = {"employee", "user"})
    Page<AttendanceRecord> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate from, LocalDate to, Pageable pageable);

    Optional<AttendanceRecord> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    boolean existsByEmployeeIdAndDate(Long employeeId, LocalDate date);
}
