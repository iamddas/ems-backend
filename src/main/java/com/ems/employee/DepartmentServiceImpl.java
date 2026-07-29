package com.ems.employee;

import com.ems.common.exception.ResourceNotFoundException;
import com.ems.employee.dto.DepartmentRequest;
import com.ems.employee.dto.DepartmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        if (departmentRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Department already exists: " + request.getName());
        }
        if (departmentRepository.existsByCode(request.getCode())) {
            throw new IllegalArgumentException("Department code already in use: " + request.getCode());
        }
        Department dept = Department.builder()
                .name(request.getName())
                .code(request.getCode().toUpperCase())
                .description(request.getDescription())
                .build();
        return toResponse(departmentRepository.save(dept));
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DepartmentResponse> getAll() {
        return departmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DepartmentResponse> getAll(Pageable pageable) {
        return departmentRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public DepartmentResponse update(Long id, DepartmentRequest request) {
        Department dept = findOrThrow(id);
        dept.setName(request.getName());
        dept.setCode(request.getCode().toUpperCase());
        dept.setDescription(request.getDescription());
        return toResponse(departmentRepository.save(dept));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Department", "id", id);
        }
        departmentRepository.deleteById(id);
    }

    private Department findOrThrow(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", "id", id));
    }

    private DepartmentResponse toResponse(Department dept) {
        return DepartmentResponse.builder()
                .id(dept.getId())
                .name(dept.getName())
                .code(dept.getCode())
                .description(dept.getDescription())
                .createdAt(dept.getCreatedAt())
                .updatedAt(dept.getUpdatedAt())
                .build();
    }
}
