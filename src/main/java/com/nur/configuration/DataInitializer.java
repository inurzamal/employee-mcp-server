package com.nur.configuration;

import com.nur.entity.Employee;
import com.nur.repository.EmployeeRepository;
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
    public CommandLineRunner seedDatabase(EmployeeRepository employeeRepository) {
        return args -> {
            if (employeeRepository.count() == 0) {
                log.info("No employees found in database. Seeding initial data...");
                List<Employee> initialEmployees = List.of(
                        new Employee("Rahul Sharma", "Engineering", "rahul@example.com"),
                        new Employee("Priya Singh", "HR", "priya@example.com"),
                        new Employee("Amit Kumar", "Engineering", "amit@example.com"),
                        new Employee("Sneha Patel", "Finance", "sneha@example.com"),
                        new Employee("Vikram Reddy", "Engineering", "vikram@example.com"),
                        new Employee("Simran", "HR", "simran@example.com")
                );
                employeeRepository.saveAll(initialEmployees);
                log.info("Successfully seeded {} employees into MySQL database.", initialEmployees.size());
            } else {
                log.info("Database already contains {} employee records. Skipping seed.", employeeRepository.count());
            }
        };
    }
}
