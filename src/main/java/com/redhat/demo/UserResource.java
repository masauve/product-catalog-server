package com.redhat.demo;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.redhat.demo.model.User;

import org.jboss.resteasy.annotations.jaxrs.PathParam;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import io.quarkus.security.Authenticated;

@Path("/api/user")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
@Tag(name = "Users", description = "An API to manipulate users of the catalog")
public class UserResource {

    @GET
    @Path("{id}")
    @Operation(summary = "Get user by ID", description = "Get specific user by it's ID")
    public User getUser(@PathParam("id") Integer id) {
        User user = User.findById(id);
        if (user == null) {
            throw new WebApplicationException("User with id of " + id + " does not exist.", 404);
        }
        return user;
    }

    @POST
    @Authenticated
    @Operation(summary = "Create user", description = "Create a new user")
    public Response create(User user) {
        if (user.id != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }
        user.persist();
        return Response.ok(user).status(201).build();
    }

    @PUT
    @Path("{id}")
    @Authenticated
    @Operation(summary = "Update user", description = "Update an existing user")
    public User update(@PathParam Integer id, User user) {
        if (user.email == null) {
            throw new WebApplicationException("User EMail was not set on request.", 422);
        }
        if (user.passwordHash == null) {
            throw new WebApplicationException("User Password was not set on request.", 422);
        }
        if (user.id == null || !user.id.equals(id)) {
            throw new WebApplicationException("User ID is not equal to persisted user ID.", 422);
        }
        user.persist();
        return user;
    }

    @DELETE
    @Path("{id}")
    @Authenticated
    @Operation(summary = "Delete user", description = "Delete a user")
    public Response delete(@PathParam Integer id) {
        User user = User.findById(id);
        if (user != null) {
            user.delete();
            return Response.status(204).build();
        } else {
            return Response.status(404).build();
        }
    }

    @Provider
    public static class ErrorMapper implements ExceptionMapper<Exception> {

        @Override
        public Response toResponse(Exception exception) {
            int code = 500;
            if (exception instanceof WebApplicationException) {
                code = ((WebApplicationException) exception).getResponse().getStatus();
            }
            return Response.status(code)
                    .entity(Json.createObjectBuilder().add("error", exception.getMessage()).add("code", code).build())
                    .build();
        }
    }
}