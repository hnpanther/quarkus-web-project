package com.hnp.resource;

import com.hnp.model.Employee;
import com.hnp.service.EmployeeService;
import io.quarkus.security.Authenticated;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/employee")
public class EmployeeResource {

    private static final Logger log = Logger.getLogger(EmployeeResource.class.getName());

//    @Inject
//    private EmployeeService service;

    private EmployeeService employeeService;

    public EmployeeResource(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
//    @RolesAllowed({"admin", "user"})
    @Authenticated
    public List<Employee> getEmployees() {
        log.log(Level.INFO, "getEmployees()");
        return employeeService.getEmployees();
    }
}
