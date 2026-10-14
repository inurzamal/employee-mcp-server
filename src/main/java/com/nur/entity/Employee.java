package com.nur.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "employees")
@Getter
@Setter
@ToString
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String department;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "job_role")
    private String role;

    @Column(length = 1000)
    private String skills;

    @Column(length = 1000)
    private String bio;

    public Employee() {
    }

    public Employee(Long id, String name, String department, String email) {
        this(id, name, department, email, null, null, null);
    }

    public Employee(String name, String department, String email) {
        this(null, name, department, email, null, null, null);
    }

    public Employee(String name, String department, String email, String role, String skills, String bio) {
        this(null, name, department, email, role, skills, bio);
    }

    public Employee(Long id, String name, String department, String email, String role, String skills, String bio) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.email = email;
        this.role = role;
        this.skills = skills;
        this.bio = bio;
    }
}
