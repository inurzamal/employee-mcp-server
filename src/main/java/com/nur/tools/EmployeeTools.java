package com.nur.tools;

import com.nur.dto.EmployeeDto;
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
 * Exposes Employee directory capabilities (including SQL filters and semantic vector search)
 * as Model Context Protocol (MCP) tools for LLM clients.
 */
@Component
@RequiredArgsConstructor
public class EmployeeTools {

    private final EmployeeService employeeService;

    @Tool(description = "Get all employees from the database. Returns the complete list of employees with their roles and skills.")
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

    @Tool(description = "Perform semantic vector similarity search to find employees based on skills, technical expertise, project experience, or bio (e.g. 'who knows Kafka and distributed systems?', 'find someone with cloud DevOps experience', 'who has corporate budgeting or tax skills?').")
    public List<EmployeeDto> searchEmployeesBySkillOrExpertise(
            @ToolParam(description = "Natural language search query describing the skills, tools, role, or background needed") String query) {
        return employeeService.searchEmployeesBySimilarity(query, 3);
    }

    @Tool(description = "Add a new employee to the database and vector store.")
    public EmployeeDto addEmployee(
            @ToolParam(description = "Full name of the employee") String name,
            @ToolParam(description = "Department name, e.g. Engineering, HR, Finance") String department,
            @ToolParam(description = "Unique corporate email address of the employee") String email,
            @ToolParam(description = "Job title or role, e.g. Senior Backend Engineer") String role,
            @ToolParam(description = "Comma-separated key technical or professional skills") String skills,
            @ToolParam(description = "Brief bio or summary of experience") String bio) {
        return employeeService.createEmployee(name, department, email, role, skills, bio);
    }

    @Tool(description = "Delete an employee by their unique employee ID from the database and vector store.")
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
