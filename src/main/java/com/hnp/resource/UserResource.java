package com.hnp.resource;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.hnp.entity.User;
import com.hnp.service.UserService;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    private static final Logger log = Logger.getLogger(UserResource.class.getName());

    @Inject
    UserService userService;

    @POST
    @RolesAllowed("ADMIN")
    public Response createUser(User user) {
        log.log(Level.INFO, "Creating User" + user.toString());
        user.password = BCrypt.withDefaults().hashToString(12, user.password.toCharArray());
        userService.createUser(user);
        return Response.status(Response.Status.CREATED).build();
    }

    @GET
    @Path("/{id}")
    @Authenticated
    public Response getUser(@PathParam("id") String id) {
        log.log(Level.INFO, "Getting User with id: " + id);
        Optional<User> user = userService.findById(id);
        return user.map(u -> Response.ok(u).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }


    @GET
    @Authenticated
    public List<User> getUsers(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("50") int size
    ) {
        log.log(Level.INFO, "Getting Users with page: " + page + " and size: " + size);
        return userService.listUsersPaginated(page, size);

    }

    @GET
    @Path("/search")
    @Authenticated
    public List<User> searchUsers(
            @QueryParam("query") String query,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("50") int size
    ) {

        log.log(Level.INFO, "Getting Users with query: " + query);
        if(query == null || query.isEmpty()) {
            log.log(Level.INFO, "Empty query");
            return userService.listUsersPaginated(page, size);
        }
        return userService.searchUsers(query, page, size);
    }


    @PUT
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response updateUser(@PathParam("id") String id, User user) {

        log.log(Level.INFO, "Updating User with id: " + id + " " + user.toString());

        Optional<User> userOptional = userService.findById(id);
        if(userOptional.isEmpty()) {
            log.log(Level.INFO, "User with id: " + id + " not found");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        log.log(Level.INFO, "Found, Updating User with id: " + id);
        user.id = userOptional.get().id;

        User existing = userOptional.get();

        if(user.username != null) existing.username = user.username;
        if(user.password != null) existing.password = BCrypt.withDefaults().hashToString(12, user.password.toCharArray());
        if(user.firstName != null) existing.firstName = user.firstName;
        if(user.lastName != null) existing.lastName = user.lastName;
        if(user.email != null) existing.email = user.email;
        if(user.roleIds != null) existing.roleIds = user.roleIds;

        User updatedUser = userService.updateUser(existing);
        return Response.ok(updatedUser).build();
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("ADMIN")
    public Response deleteUser(@PathParam("id") String id) {

        log.log(Level.INFO, "Deleting User with id: " + id);

        boolean deleted = userService.deleteUser(id);
        if(deleted) {
            log.log(Level.INFO, "User with id: " + id + " deleted");
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }



}
