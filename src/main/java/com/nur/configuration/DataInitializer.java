package com.nur.configuration;

import com.nur.entity.Employee;
import com.nur.repository.EmployeeRepository;
import com.nur.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner seedDatabase(EmployeeRepository employeeRepository, EmployeeService employeeService) {
        return args -> {
            if (employeeRepository.count() == 0) {
                log.info("No employees found in database. Seeding initial data with roles, skills, and bios...");
                List<Employee> initialEmployees = List.of(
                        new Employee("Rahul Sharma", "Engineering", "rahul@example.com",
                                "Senior Backend Architect",
                                "Java, Spring Boot, Microservices, Apache Kafka, Distributed Systems, Redis, MySQL",
                                "8+ years designing low-latency event-driven microservices, payment gateways, and scalable architectures."),
                        new Employee("Priya Singh", "HR", "priya@example.com",
                                "HR Business Partner & Talent Lead",
                                "Technical Recruiting, Performance Management, Employee Relations, DEI, Team Building",
                                "Strategic people partner focused on scaling high-performing engineering teams and company culture."),
                        new Employee("Amit Kumar", "Engineering", "amit@example.com",
                                "Full Stack & Cloud Developer",
                                "React, TypeScript, Node.js, Spring Boot, Docker, AWS, Kubernetes, REST APIs",
                                "Builds responsive frontend interfaces and scalable containerized web applications on cloud infrastructure."),
                        new Employee("Sneha Patel", "Finance", "sneha@example.com",
                                "Financial Controller",
                                "Financial Modeling, Corporate Budgeting, Risk Analysis, Tax Compliance, QuickBooks, Auditing",
                                "Manages corporate fiscal strategies, quarterly budget allocations, risk mitigation, and financial audits."),
                        new Employee("Vikram Reddy", "Engineering", "vikram@example.com",
                                "Site Reliability Engineer (SRE)",
                                "DevOps, Kubernetes, Terraform, Prometheus, CI/CD, Linux, Cloud Security, Automation",
                                "Specializes in infrastructure automation, system observability, cloud security, and ensuring 99.99% uptime."),
                        new Employee("Simran", "HR", "simran@example.com",
                                "People Operations Specialist",
                                "Onboarding, Compensation & Benefits, HRIS, Employee Engagement, Workplace Policies",
                                "Streamlines internal operations, company benefits administration, and delightful new-hire onboarding experiences.")
                );
                employeeRepository.saveAll(initialEmployees);
                log.info("Successfully seeded {} employees into MySQL database.", initialEmployees.size());
            }

            // Index existing employees into SimpleVectorStore for semantic search
            try {
                List<Employee> allEmployees = employeeRepository.findAll();
                employeeService.indexAllEmployees(allEmployees);
                log.info("Successfully indexed {} employees in SimpleVectorStore for semantic search.", allEmployees.size());
            } catch (Exception ex) {
                log.warn("Vector store indexing on startup deferred or failed: {}", ex.getMessage());
            }
        };
    }
}
