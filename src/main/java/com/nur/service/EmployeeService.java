package com.nur.service;

import com.nur.dto.EmployeeDto;

import java.util.List;
import java.util.Optional;

public interface EmployeeService {

    List<EmployeeDto> getAllEmployees();

    List<String> getAllDepartments();

    Optional<EmployeeDto> getEmployeeById(Long id);

    List<EmployeeDto> searchEmployeesByDepartment(String department);

    EmployeeDto createEmployee(String name, String department, String email);

    void deleteEmployee(Long id);
}
