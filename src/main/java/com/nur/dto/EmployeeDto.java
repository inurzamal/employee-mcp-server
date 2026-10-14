package com.nur.dto;

/**
 * Immutable Data Transfer Object representing an employee.
 * Decouples the MCP tool/client presentation layer from JPA entity internals.
 */
public record EmployeeDto(
        Long id,
        String name,
        String department,
        String email
) {
}
