package com.hnp.resource;

import com.hnp.service.RedisService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/redis")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RedisResource {

    private static final Logger log = Logger.getLogger(RedisService.class.getName());

    @Inject
    RedisService redisService;

    @POST
    @Path("/set")
    public Uni<Response> setKey(@QueryParam("key") String key,
                                @QueryParam("value") String value,
                                @QueryParam("ttl") @DefaultValue("0") long ttlSeconds) {

        log.log(Level.INFO, "Set key in Redis Resource with key: " + key + " and value: " + value + " and ttl: " + ttlSeconds);
        Uni<Void> operation;
        if(ttlSeconds > 0) {
            operation = redisService.setEx(key, value, ttlSeconds);
        } else {
            operation = redisService.set(key, value);
        }

        return operation
                .onItem().transform(v -> Response.ok("Key set sucessfully").build())
                .onFailure().recoverWithItem(e -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(e.getMessage()).build());

    }

    @GET
    @Path("/get")
    public Uni<Response> getKey(@QueryParam("key") String key) {
        log.log(Level.INFO, "Get key in Redis Resource with key: " + key);
        return redisService.get(key)
                .onItem().transform(v -> {
                    if (v == null) {
                        return Response.status(Response.Status.NOT_FOUND).build();
                    }
                    return Response.ok(v).build();
                })
                .onFailure().recoverWithItem(e -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(e.getMessage()).build());
    }

    @DELETE
    @Path("/delete")
    public Uni<Response> deleteKey(@QueryParam("key") String key) {
        log.log(Level.INFO, "Delete key in Redis Resource with key: " + key);
        return redisService.delete(key).onItem().transform(v -> Response.ok("Key deleted successfully").build())
                .onFailure().recoverWithItem(e -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(e.getMessage()).build());
    }

    @GET
    @Path("/exists")
    public Uni<Response> existsKey(@QueryParam("key") String key) {
        log.log(Level.INFO, "Check Exists key in Redis Resource with key: " + key);
        return redisService.exists(key).onItem().transform(v -> Response.ok("Key " + v + " exists").build())
                .onFailure().recoverWithItem(e -> Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(e.getMessage()).build());
    }
}
