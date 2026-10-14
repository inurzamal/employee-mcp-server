package com.nur.dto;

/**
 * Immutable Data Transfer Object representing an employee.
 * Exposes role, skills, and bio alongside core attributes for rich LLM context and semantic search.
 */
public record EmployeeDto(
        Long id,
        String name,
        String department,
        String email,
        String role,
        String skills,
        String bio
) {
}
