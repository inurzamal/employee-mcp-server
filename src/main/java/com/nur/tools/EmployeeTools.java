package com.nur.tools;

import com.nur.dto.EmployeeDto;
import com.nur.exception.DuplicateEmailException;
import com.nur.exception.EmployeeNotFoundException;
import com.nur.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * MCP Tool Adapter Layer.
 * Exposes Employee business capabilities as Model Context Protocol (MCP) tools for LLM clients.
 * Delegates all business logic and data access to the EmployeeService.
 */
@Component
@RequiredArgsConstructor
public class EmployeeTools {

    private final EmployeeService employeeService;

    @Tool(description = "Get all employees from the database. Returns the complete list of employees.")
    public List<EmployeeDto> listEmployees() {
        return employeeService.getAllEmployees();
    }

    @Tool(description = "Get all available department names from the database. Call this first to know the exact department names before searching by department.")
    public List<String> listDepartments() {
        return employeeService.getAllDepartments();
    }

    @Tool(description = "Find an employee by their unique employee ID from the database.")
    public Optional<EmployeeDto> getEmployeeById(
            @ToolParam(description = "The unique numeric ID of the employee") Long employeeId) {
        return employeeService.getEmployeeById(employeeId);
    }

    @Tool(description = "Search employees by department name from the database. Standard departments are: Engineering, HR, Finance. Use listDepartments tool first if unsure.")
    public List<EmployeeDto> searchEmployeesByDepartment(
            @ToolParam(description = "The department name, e.g. Engineering, HR, Finance") String department) {
        return employeeService.searchEmployeesByDepartment(department);
    }

    @Tool(description = "Add a new employee to the database.")
    public EmployeeDto addEmployee(
            @ToolParam(description = "Full name of the employee") String name,
            @ToolParam(description = "Department name, e.g. Engineering, HR, Finance") String department,
            @ToolParam(description = "Unique corporate email address of the employee") String email) {
        return employeeService.createEmployee(name, department, email);
    }

    @Tool(description = "Delete an employee by their unique employee ID from the database.")
    public String deleteEmployeeById(
            @ToolParam(description = "The unique numeric ID of the employee to delete") Long employeeId) {
        try {
            employeeService.deleteEmployee(employeeId);
            return "Employee with ID " + employeeId + " was successfully deleted.";
        } catch (EmployeeNotFoundException | IllegalArgumentException ex) {
            return ex.getMessage();
        }
    }
}
