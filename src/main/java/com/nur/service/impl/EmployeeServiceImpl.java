package com.nur.service.impl;

import com.nur.dto.EmployeeDto;
import com.nur.entity.Employee;
import com.nur.exception.DuplicateEmailException;
import com.nur.exception.EmployeeNotFoundException;
import com.nur.repository.EmployeeRepository;
import com.nur.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;

@Service
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger log = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    private static final Map<String, String> DEPARTMENT_ALIASES = Map.ofEntries(
            entry("human resources", "HR"),
            entry("human resource", "HR"),
            entry("people ops", "HR"),
            entry("hr", "HR"),
            entry("accounting", "Finance"),
            entry("accounts", "Finance"),
            entry("finance", "Finance"),
            entry("financial", "Finance"),
            entry("eng", "Engineering"),
            entry("development", "Engineering"),
            entry("dev", "Engineering"),
            entry("engineering", "Engineering"),
            entry("tech", "Engineering")
    );

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public List<EmployeeDto> getAllEmployees() {
        log.debug("Fetching all employees from database");
        return employeeRepository.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<String> getAllDepartments() {
        log.debug("Fetching all distinct departments from database");
        return employeeRepository.findDistinctDepartments();
    }

    @Override
    public Optional<EmployeeDto> getEmployeeById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        log.debug("Fetching employee by id: {}", id);
        return employeeRepository.findById(id).map(this::toDto);
    }

    @Override
    public List<EmployeeDto> searchEmployeesByDepartment(String department) {
        if (department == null || department.trim().isEmpty()) {
            return List.of();
        }
        String resolvedDepartment = resolveDepartmentName(department);
        log.info("Searching employees for resolved department: '{}' (original input: '{}')",
                resolvedDepartment, department);
        return employeeRepository.findByDepartmentIgnoreCase(resolvedDepartment).stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EmployeeDto createEmployee(String name, String department, String email) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name cannot be blank.");
        }
        if (department == null || department.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee department cannot be blank.");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Employee email cannot be blank.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        if (employeeRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException("Employee with email '" + normalizedEmail + "' already exists.");
        }

        String resolvedDepartment = resolveDepartmentName(department);
        Employee employee = new Employee(name.trim(), resolvedDepartment, normalizedEmail);
        Employee saved = employeeRepository.save(employee);
        log.info("Successfully created employee with id: {}", saved.getId());
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Employee ID cannot be null.");
        }
        if (!employeeRepository.existsById(id)) {
            throw new EmployeeNotFoundException("Employee with ID " + id + " was not found.");
        }
        employeeRepository.deleteById(id);
        log.info("Successfully deleted employee with id: {}", id);
    }

    private String resolveDepartmentName(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
        // Strip common conversational suffixes like "department" or "dept"
        String cleaned = input.trim()
                .replaceAll("(?i)\\b(department|dept)\\b", "")
                .trim()
                .toLowerCase();

        String alias = DEPARTMENT_ALIASES.get(cleaned);
        return alias != null ? alias : input.trim();
    }

    private EmployeeDto toDto(Employee entity) {
        return new EmployeeDto(
                entity.getId(),
                entity.getName(),
                entity.getDepartment(),
                entity.getEmail()
        );
    }
}
