package com.hnp.resource;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/resource")
public class MyResource {

    private static final Logger log = Logger.getLogger(MyResource.class.getName());

    @Inject
    SecurityIdentity identity;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello From Quarkus";
    }

    @GET
    @Path("/me")
    public String me() {
        log.log(Level.INFO, "me called");
        String username = identity.getPrincipal().getName();
        log.log(Level.INFO, "username is " + username);
        return username;
    }
}
