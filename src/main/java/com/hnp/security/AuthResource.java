package com.hnp.security;

import com.hnp.resource.EmployeeResource;
import io.quarkus.runtime.annotations.RegisterForReflection;
import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.build.Jwt;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger log = Logger.getLogger(AuthResource.class.getName());


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
    public Response login(LoginRequest loginRequest) throws Exception {

        PrivateKey privateKey = loadPrivateKey();

        if (loginRequest.username == null || loginRequest.password == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        if ("admin".equals(loginRequest.username) && "admin123".equals(loginRequest.password)) {
            Set<String> roles = new HashSet<String>();
            roles.add("admin");
            String token = Jwt.issuer("quarkus-app")
                    .upn(loginRequest.username)
                    .groups(roles)
                    .sign(privateKey);
            return Response.ok(new LoginResponse(token)).build();
        }
        if ("user".equals(loginRequest.username) && "user123".equals(loginRequest.password)) {
            Set<String> roles = new HashSet<String>();
            roles.add("user");
            String token = Jwt.issuer("quarkus-app")
                    .upn(loginRequest.username)
                    .groups(roles)
                    .sign();
            return Response.ok(new LoginResponse(token)).build();
        }

        return Response.status(Response.Status.UNAUTHORIZED).build();
    }
}
