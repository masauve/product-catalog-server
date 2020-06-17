package com.redhat.demo;

import javax.annotation.security.PermitAll;
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

import com.redhat.demo.model.Category;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import org.jboss.resteasy.annotations.jaxrs.PathParam;

import io.quarkus.security.Authenticated;

@Path("/api/category")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
@Tag(name = "Categories", description = "An API to manipulate the categories in the catalog")
public class CategoryResource {

    @GET
    @PermitAll
    public Category[] get() {
        return Category.listAll().toArray(new Category[0]);
    }

    @GET
    @Path("{id}")
    @PermitAll
    @Operation(summary = "Get category", description = "Get specific category by ID")
    @Counted(name = "countGetCategorybyId", description = "How many get category by ID calls have been performed.", tags = {"type=counter", "api=category", "method=getCategoryById"})
    @Timed(name = "perfGetCategoryById", description = "A measure of how long it takes to get category by ID.", unit = MetricUnits.MILLISECONDS, tags = {"type=perf", "api=category", "method=getCategoryById"})
    public Category getCategory(@PathParam("id") Integer id) {
        Category category = Category.findById(id);
        if (category == null) {
            throw new WebApplicationException("Category with id of " + id + " does not exist.", 404);
        }
        return category;
    }

    @POST
    @Authenticated
    @Operation(summary = "Create category", description = "Create a new category")
    @Counted(name = "countCreateCategory", description = "How many create category calls have been performed.", tags = {"type=counter", "api=category", "method=createCategory"})
    @Timed(name = "perfCreateCategory", description = "A measure of how long it takes to create a category.", unit = MetricUnits.MILLISECONDS, tags = {"type=perf", "api=category", "method=createCategory"})
    public Response create(Category category) {
        try {
            category.persist();
            return Response.ok(category).status(201).build();
        } catch (Exception e) {
            return Response.serverError().status(500).build();
        }
    }

    @PUT
    @Path("{id}")
    @Authenticated
    @Operation(summary = "Update category", description = "Update an existing category")
    @Counted(name = "countUpdateCategory", description = "How many update category calls have been performed.", tags = {"type=counter", "api=category", "method=updateCategory"})
    @Timed(name = "perfUpdateCategory", description = "A measure of how long it takes to update a category.", unit = MetricUnits.MILLISECONDS, tags = {"type=perf", "api=category", "method=updateCategory"})
    public Category update(@PathParam Integer id, Category category) {
        if (category.name == null) {
            throw new WebApplicationException("Category Name was not set on request.", 422);
        }
        if (category.id == null || !category.id.equals(id)) {
            throw new WebApplicationException("Category ID is not equal to persisted category ID.", 422);
        }
        category.persist();
        return category;
    }

    @DELETE
    @Path("{id}")
    @Authenticated
    @Operation(summary = "Delete category", description = "Delete a category")
    @Counted(name = "countDeleteCategory", description = "How many delete category calls have been performed.", tags = {"type=counter", "api=category", "method=deleteCategory"})
    @Timed(name = "perfDeleteCategory", description = "A measure of how long it takes to delete a category.", unit = MetricUnits.MILLISECONDS, tags = {"type=perf", "api=category", "method=deleteCategory"})
    public Response delete(@PathParam Integer id) {
        Category category = Category.findById(id);
        if (category != null) {
            category.delete();
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