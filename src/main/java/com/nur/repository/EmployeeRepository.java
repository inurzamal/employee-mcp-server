package com.nur.repository;

import com.nur.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findByDepartmentIgnoreCase(String department);

    @Query("SELECT DISTINCT e.department FROM Employee e")
    List<String> findDistinctDepartments();

    boolean existsByEmail(String email);
}
