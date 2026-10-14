package com.nur.service.impl;

import com.nur.dto.EmployeeDto;
import com.nur.entity.Employee;
import com.nur.exception.DuplicateEmailException;
import com.nur.exception.EmployeeNotFoundException;
import com.nur.repository.EmployeeRepository;
import com.nur.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final VectorStore vectorStore;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, VectorStore vectorStore) {
        this.employeeRepository = employeeRepository;
        this.vectorStore = vectorStore;
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
    public EmployeeDto createEmployee(String name, String department, String email, String role, String skills, String bio) {
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
        Employee employee = new Employee(
                name.trim(),
                resolvedDepartment,
                normalizedEmail,
                role != null ? role.trim() : null,
                skills != null ? skills.trim() : null,
                bio != null ? bio.trim() : null
        );
        Employee saved = employeeRepository.save(employee);
        log.info("Successfully created employee with id: {}", saved.getId());

        // Index in Vector Store for similarity search
        try {
            indexEmployee(saved);
        } catch (Exception ex) {
            log.warn("Failed to index newly created employee in vector store: {}", ex.getMessage());
        }

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

        // Remove from Vector Store
        try {
            vectorStore.delete(List.of(String.valueOf(id)));
        } catch (Exception ex) {
            log.warn("Failed to remove employee {} from vector store: {}", id, ex.getMessage());
        }
    }

    @Override
    public List<EmployeeDto> searchEmployeesBySimilarity(String query, int topK) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        int limit = topK > 0 ? topK : 3;
        log.info("Performing vector similarity search for query: '{}', topK: {}", query, limit);

        SearchRequest request = SearchRequest.builder()
                .query(query.trim())
                .topK(limit)
                .build();

        List<Document> similarDocuments = vectorStore.similaritySearch(request);
        if (similarDocuments.isEmpty()) {
            log.info("No matching employees found in vector store for query: '{}'", query);
            return List.of();
        }

        List<Long> employeeIds = similarDocuments.stream()
                .map(Document::getId)
                .filter(Objects::nonNull)
                .map(Long::valueOf)
                .toList();

        Map<Long, Employee> employeeMap = employeeRepository.findAllById(employeeIds).stream()
                .collect(Collectors.toMap(Employee::getId, e -> e));

        // Preserve vector ranking order
        return employeeIds.stream()
                .map(employeeMap::get)
                .filter(Objects::nonNull)
                .map(this::toDto)
                .toList();
    }

    @Override
    public void indexEmployee(Employee employee) {
        if (employee == null || employee.getId() == null) {
            return;
        }
        Document document = toDocument(employee);
        vectorStore.add(List.of(document));
        log.debug("Indexed employee {} in vector store", employee.getId());
    }

    @Override
    public void indexAllEmployees(List<Employee> employees) {
        if (employees == null || employees.isEmpty()) {
            return;
        }
        List<Document> documents = employees.stream()
                .filter(e -> e.getId() != null)
                .map(this::toDocument)
                .toList();
        vectorStore.add(documents);
        log.info("Indexed {} employees into SimpleVectorStore", documents.size());
    }

    private Document toDocument(Employee employee) {
        String content = """
                Name: %s
                Department: %s
                Role: %s
                Skills: %s
                Bio: %s
                """.formatted(
                employee.getName(),
                employee.getDepartment(),
                employee.getRole() != null ? employee.getRole() : "Not specified",
                employee.getSkills() != null ? employee.getSkills() : "Not specified",
                employee.getBio() != null ? employee.getBio() : "Not specified"
        );

        Map<String, Object> metadata = Map.of(
                "employeeId", employee.getId(),
                "department", employee.getDepartment(),
                "email", employee.getEmail()
        );

        return Document.builder()
                .id(String.valueOf(employee.getId()))
                .text(content)
                .metadata(metadata)
                .build();
    }

    private String resolveDepartmentName(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }
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
                entity.getEmail(),
                entity.getRole(),
                entity.getSkills(),
                entity.getBio()
        );
    }
}
