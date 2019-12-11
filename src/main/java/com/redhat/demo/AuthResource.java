package com.redhat.demo;

import java.security.Principal;
import java.time.LocalDateTime;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.redhat.demo.model.User;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/api/auth")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "An API to manage user authentication and authorization")
public class AuthResource {

    private static final Logger log = LoggerFactory.getLogger("AuthResource");

    @GET
    @Path("/user")
    @PermitAll
    @Operation(summary = "Get user", description = "Get the current user")
    public User getUser(@Context SecurityContext sc) {
        if (sc.getUserPrincipal() !=null) {
            return User.find("email", sc.getUserPrincipal().toString()).firstResult();
        } else {
            return new User();
        }
    }

    @POST
    @PermitAll
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Login", description = "Authenticate a user")
    public LoginResult authenticate(@Context SecurityContext sc) {
        Principal userPrincipal = sc.getUserPrincipal();
        if (userPrincipal !=null) {
            User user = User.find("email", sc.getUserPrincipal().toString()).firstResult();
            if (user != null) {
                log.info("Login succeeded for user " + user.email);
                return new LoginResult("Login Success", user);
            } else {
                return new LoginResult("Invalid user/password", null);
            }
        } else {
            return new LoginResult("Authentication failed", null);
        }
    }


    @POST
    @Path("/register")
    @PermitAll
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    @Operation(summary = "Register", description = "Register a new user")
    public Response register(@FormParam("email") String email,
                         @FormParam("password") String password,
                         @FormParam("password_confirmation") String passwordConfirmation) {

        return Response.status(Response.Status.OK).entity("User registration is not supported at this time").build();

        // Can't compile wildfly code needed to generate passwords with quarkus native compiler
        /*
        if (password == null || password.isEmpty()) {
            throw new WebApplicationException("Password must be provided");
        }
        if (!password.equals(passwordConfirmation)) {
            throw new WebApplicationException("Password and password confirmation must match");
        }

        try {
            User user = new User();
            user.email = email;
            user.setPasswordHash(password);

            user.createdAt = LocalDateTime.now();
            user.persist();
            return Response.status(Response.Status.OK).entity("true").build();
        } catch (Exception e) {
            e.printStackTrace();
            throw new WebApplicationException("User could not be saved", 500);
        }
        */

    }

    @Provider
    public static class ErrorMapper implements ExceptionMapper<Exception> {

        @Override
        public Response toResponse(Exception exception) {
            int code = 500;
            if (exception instanceof WebApplicationException) {
                code = ((WebApplicationException) exception).getResponse().getStatus();
            }
            return Response.status(code).entity(exception.getMessage()).build();
            //        .entity(Json.createObjectBuilder().add("error", exception.getMessage()).add("code", code).build())
            //        .build();
        }
    }

    public class LoginResult {
        public String message;
        public SecureUser user;

        public LoginResult() {}

        public LoginResult(String message, User user) {
            this.message = message;
            this.user = new SecureUser(user);
        }

    }

    public class SecureUser {
        public String email;
        public LocalDateTime createdAt;

        public SecureUser (User user) {
            this.email = user.email;
            this.createdAt = user.createdAt;
        }

    }
}