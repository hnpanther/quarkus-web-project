package com.hnp.service;

import com.hnp.entity.Employee;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class EmployeeService {

    private static final Logger log = Logger.getLogger(EmployeeService.class.getName());

    private final List<Employee> employees = new ArrayList<>();


    @PostConstruct
    void init() {
        log.log(Level.INFO, "Employee Service init");
        for(long i = 1; i <= 10; i++) {
            employees.add(new Employee(
                    i,
                    "E" + String.format("%03d", i),
                    "FirstName" + i,
                    "LastName" + i
            ));
        }
    }

    public List<Employee> getEmployees() {
        log.log(Level.INFO, "Employee Service getEmployees");
        return employees;
    }
}
