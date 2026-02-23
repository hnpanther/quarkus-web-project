package com.hnp.security;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.hnp.entity.Role;
import com.hnp.entity.User;
import com.hnp.service.RedisService;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.jwt.build.Jwt;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;


import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Duration;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import jakarta.inject.Inject;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger log = Logger.getLogger(AuthResource.class.getName());

    @Inject
    RedisService redisService;


    public static class LoginRequest {
        public String username;
        public String password;
    }

    @RegisterForReflection
    public static class LoginResponse {
        public String token;
        public LoginResponse(String token) { this.token = token; }
    }

    public PrivateKey loadPrivateKey() throws Exception {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("keys/privateKey.pem")) {
            if (is == null) throw new RuntimeException("Private key not found in classpath!");

            String privateKeyPem = new String(is.readAllBytes(), StandardCharsets.UTF_8);

            privateKeyPem = privateKeyPem
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] decodedKey = Base64.getDecoder().decode(privateKeyPem);

            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decodedKey));
        }
    }

    @POST
    @Path("/login")
    public Uni<Response> login(LoginRequest loginRequest) throws Exception {
        log.log(Level.INFO, "Login Request: " + loginRequest);

        PrivateKey privateKey = loadPrivateKey();

        if (loginRequest.username == null || loginRequest.password == null) {
            log.log(Level.WARNING, "Login Request: username or password is null!");
            return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).build());
        }

        User user = User.find("username", loginRequest.username).firstResult();

        if (user == null) {
            log.log(Level.WARNING, "Login Request: user not found!");
            return Uni.createFrom().item(Response.status(Response.Status.UNAUTHORIZED).build());
        }

        BCrypt.Result result = BCrypt.verifyer().verify(loginRequest.password.toCharArray(), user.password.toCharArray());

        if (!result.verified) {
            log.log(Level.WARNING, "Login Request: password verification failed!");
            return Uni.createFrom().item(Response.status(Response.Status.UNAUTHORIZED).build());
        }

        List<Role> roles = Role.list("_id in ?1", user.roleIds);
        Set<String> roleNames = roles.stream()
                .map(r -> r.roleName)
                .collect(Collectors.toSet());
        String jti = UUID.randomUUID().toString();
        String token = Jwt.issuer("quarkus-app")
                .upn(loginRequest.username)
                .groups(roleNames)
                .claim("jti", jti)
                .expiresIn(Duration.ofMinutes(30))
                .sign(privateKey);
        log.log(Level.INFO, "Login Request: jti: " + jti);


        String redisKey = "user:" + loginRequest.username;

        return redisService.get(redisKey)
                .onItem().transformToUni(prevJti -> {
                    if (prevJti != null) {
                        log.info("Previous JWT found for user: " + loginRequest.username + ", jti: " + prevJti);
                        return redisService.delete(redisKey)
                                .onItem().invoke(unused ->
                                        log.info("Previous JWT deleted for user: " + loginRequest.username + ", jti: " + prevJti)
                                );
                    } else {
                        return Uni.createFrom().voidItem();
                    }
                })
                .onItem().transformToUni(unused ->
                        redisService.setEx(redisKey, jti, 30 * 60)
                                .onItem().invoke(__ -> log.info("JWT saved in Redis for user: " + loginRequest.username + ", jti: " + jti))
                )
                .onItem().transform(unused -> Response.ok(new LoginResponse(token)).build())
                .onFailure().recoverWithItem(th -> {
                    log.warning("Login failed to save JWT: " + th.getMessage());
                    return Response.status(Response.Status.UNAUTHORIZED).build();
                });




    }
}
