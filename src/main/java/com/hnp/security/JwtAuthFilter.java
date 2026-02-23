package com.hnp.security;

import com.hnp.service.RedisService;
import io.quarkus.security.Authenticated;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTParser;
import io.smallrye.jwt.build.Jwt;
import jakarta.annotation.Priority;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.jwt.JsonWebToken;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class JwtAuthFilter implements ContainerRequestFilter {

    private static final Logger log = Logger.getLogger(JwtAuthFilter.class.getName());

    @Context
    ResourceInfo resourceInfo;

    @Inject
    JWTParser jwtParser;

    @Inject
    RedisService redisService;

    public JwtAuthFilter() {

    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {

        log.log(Level.FINEST, "Filtering request(Jwt Filter)");
//        RolesAllowed rolesAllowed = resourceInfo.getResourceMethod().getAnnotation(RolesAllowed.class);
//        Authenticated authenticated = resourceInfo.getResourceMethod().getAnnotation(Authenticated.class);

        boolean hasRolesAllowed = resourceInfo.getResourceMethod().isAnnotationPresent(RolesAllowed.class)
                || resourceInfo.getResourceClass().isAnnotationPresent(RolesAllowed.class);

        boolean hasAuthenticated = resourceInfo.getResourceMethod().isAnnotationPresent(Authenticated.class)
                || resourceInfo.getResourceClass().isAnnotationPresent(Authenticated.class);

        if(!hasAuthenticated &&  !hasRolesAllowed) {
            log.log(Level.INFO, "RolesAllowed or Authenticated annotation is null");
            return;
        }

        String authHeader = containerRequestContext.getHeaderString("Authorization");
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.log(Level.INFO, "Authorization header not found");
            containerRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }

        String token = authHeader.substring("Bearer ".length());
        log.log(Level.INFO, "Token: " + token);


        JsonWebToken jwt;
        try {
            jwt = (JsonWebToken) jwtParser.parse(token);
            log.log(Level.INFO, "JWT Token jti: " + jwt.getClaim("jti"));
        } catch (Exception e) {
            log.log(Level.INFO, "Invalid JWT token: " + token);
            containerRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }



        String username = jwt.getClaim("upn");
        String jti = jwt.getClaim("jti");
        String redisKey = "user:" + username;
        String savedJti = redisService.get(redisKey).await().indefinitely();

        if (!jti.equals(savedJti)) {
            containerRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        }



    }
}
