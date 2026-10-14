package com.nur.service;

import com.nur.dto.EmployeeDto;
import com.nur.entity.Employee;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {

    List<EmployeeDto> getAllEmployees();

    List<String> getAllDepartments();

    Optional<EmployeeDto> getEmployeeById(Long id);

    List<EmployeeDto> searchEmployeesByDepartment(String department);

    EmployeeDto createEmployee(String name, String department, String email, String role, String skills, String bio);

    void deleteEmployee(Long id);

    List<EmployeeDto> searchEmployeesBySimilarity(String query, int topK);

    void indexEmployee(Employee employee);

    void indexAllEmployees(List<Employee> employees);
}
