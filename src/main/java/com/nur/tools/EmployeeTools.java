package com.nur.tools;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class EmployeeTools {

    private static final Map<String, String> DEPARTMENT_ALIASES = Map.of(
            "human resources", "HR",
            "human resource", "HR",
            "people ops", "HR",
            "accounting", "Finance",
            "accounts", "Finance",
            "eng", "Engineering",
            "development", "Engineering",
            "dev", "Engineering"
    );

    private final List<Employee> employees = List.of(
            new Employee(1L, "Rahul Sharma", "Engineering", "rahul@example.com"),
            new Employee(2L, "Priya Singh", "HR", "priya@example.com"),
            new Employee(3L, "Amit Kumar", "Engineering", "amit@example.com"),
            new Employee(4L, "Sneha Patel", "Finance", "sneha@example.com"),
            new Employee(5L, "Vikram Reddy", "Engineering", "vikram@example.com"),
            new Employee(6L, "Simran", "HR", "simran@example.com")
    );

    @Tool(description = "Get all employees. Returns the complete list of employees.")
    public List<Employee> listEmployees() {
        return employees;
    }

    @Tool(description = "Get all available department names. Call this first to know the exact department names before searching by department.")
    public List<String> listDepartments() {
        return employees.stream()
                .map(Employee::department)
                .distinct()
                .toList();
    }

    @Tool(description = "Find an employee by their unique employee ID.")
    public Optional<Employee> getEmployeeById(
            @ToolParam(description = "The unique ID of the employee") Long employeeId) {

        return employees.stream()
                .filter(employee -> employee.id().equals(employeeId))
                .findFirst();
    }

    @Tool(description = "Search employees by department name. The exact department names are: Engineering, HR, Finance. Use listDepartments tool first if unsure about department names.")
    public List<Employee> searchEmployeesByDepartment(
            @ToolParam(description = "The exact department name, e.g. Engineering, HR, Finance") String department) {

        String resolvedDepartment = resolveDepartmentName(department);

        return employees.stream()
                .filter(employee -> employee.department().equalsIgnoreCase(resolvedDepartment))
                .toList();
    }

    private String resolveDepartmentName(String input) {
        // Check aliases for common LLM variations
        String alias = DEPARTMENT_ALIASES.get(input.toLowerCase().trim());
        return alias != null ? alias : input;
    }

    public record Employee(
            Long id,
            String name,
            String department,
            String email) {
    }
}

