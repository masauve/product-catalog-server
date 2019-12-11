package com.redhat.demo;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.json.Json;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import com.redhat.demo.model.Category;
import com.redhat.demo.model.Product;

import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.Timed;

import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import io.quarkus.security.Authenticated;

@Path("/api/product")
@ApplicationScoped
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Products", description = "An API to manipulate the products in the catalog")
public class ProductResource {

    private static final Logger log = LoggerFactory.getLogger("ProductResource");


    @GET
    @Operation(summary = "Get product list", description = "Get product list with support for paging and ordering")
    @Counted(name = "countGetProduct", description = "How many get product calls have been performed.")
    @Timed(name = "perfGetProduct", description = "A measure of how long it takes to get products.", unit = MetricUnits.MILLISECONDS)
    public Product[] get(
        @QueryParam("name") @DefaultValue("") String name,
        @QueryParam("order_by") @DefaultValue("name") String orderBy,
        @QueryParam("order_type") @DefaultValue("asc") String orderType,
        @QueryParam("page") @DefaultValue("0") Integer page,
        @QueryParam("item_per_page") @DefaultValue("0") Integer pageSize) {

        PanacheQuery<Product> query;

        if (name != null && !name.isEmpty()) {
            query = Product.find("name LIKE :name", Sort.by(orderBy).direction(orderType=="asc"? Direction.Ascending: Direction.Descending), Parameters.with("name", "%"+name+"%"));
        } else {
            query = Product.find("order by " + orderBy + " " + orderType);
        }

        List<Product> products;
        if (pageSize > 0) {
            products = query.page(Page.of(page - 1, pageSize)).list();
        } else {
            products = query.list();
        }
        return products.toArray(new Product[0]);
    }


    @GET
    @Path("{id}")
    @Operation(summary = "Get product by ID", description = "Get specific product by it's ID")
    @Counted(name = "countGetProductbyId", description = "How many get product by ID calls have been performed.")
    @Timed(name = "perfGetProductById", description = "A measure of how long it takes to get product by ID.", unit = MetricUnits.MILLISECONDS)
    public Product get(@PathParam("id") Integer id) {
        Product product = Product.findById(id);
        if (product == null) {
            throw new WebApplicationException("Product with id of " + id + " does not exist.", 404);
        }
        return product;
    }

    @GET
    @Path("/count")
    @Produces(MediaType.TEXT_PLAIN)
    @Operation(summary = "Get product count", description = "Get the total count of products available")
    @Counted(name = "countGetCount", description = "How many get product count calls have been performed.")
    @Timed(name = "perfGetCount", description = "A measure of how long it takes to get product count.", unit = MetricUnits.MILLISECONDS)
    public Response getCount() {
        Long count = Product.count();
        return Response.status(Response.Status.OK).entity(Long.toString(count)).build();
    }

    @POST
    @Authenticated
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    @Operation(summary = "Create product", description = "Create a new product")
    @Counted(name = "countCreateProduct", description = "How many get product create calls have been performed.")
    @Timed(name = "perfCreateProduct", description = "A measure of how long it takes to create a product.", unit = MetricUnits.MILLISECONDS)
    public ProductResult create(
        @FormParam("name") String name,
        @FormParam("description") String description,
        @FormParam("category_id") Integer categoryId,
        @FormParam("price") Double price
        ) {

        Category category = Category.findById(categoryId);
        if (category == null) {
            return new ProductResult(false, "Category for id " + categoryId +" was not found", null);
        }
        Product product = new Product();

        try {
            product.name = name;
            product.description = description;
            product.category = category;
            product.price = price;
            product.created = new Date(System.currentTimeMillis());
            product.modified = LocalDateTime.now();

            product.persist();

            log.info("Product id is " + product.id);
            return new ProductResult(true, "Product Created", product);
        } catch (Exception e) {
            log.error("Failed to create product " + product , e);
            return new ProductResult(false, "Error: " + e.getMessage(), null);
        }
    }

    @POST
    @Path("/delete")
    @Authenticated
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    @Operation(summary = "Delete a set of products", description = "Delete a set of products as specified by their IDs")
    @Counted(name = "countMassDelete", description = "How many get mass product delete calls have been performed.")
    @Timed(name = "perfMassDelete", description = "A measure of how long it takes to mass delete products.", unit = MetricUnits.MILLISECONDS)
    public Response massDelete(@FormParam("del_ids[]") List<Integer> productIds) {
        System.out.println(productIds.size());
        try {
            for(Integer id: productIds) {
                Product.delete("id", id);
            }
            return Response.status(Response.Status.OK).entity("true").build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(500).entity("false").build();
        }

    }

    @PUT
    @Path("{id}")
    @Authenticated
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Transactional
    @Operation(summary = "Update product", description = "Update an existing product")
    @Counted(name = "countUpdateProduct", description = "How many update product calls have been performed.")
    @Timed(name = "perfUpdateProduct", description = "A measure of how long it takes to update product.", unit = MetricUnits.MILLISECONDS)
    public ProductResult update(
        @PathParam("id") Integer id,
        @FormParam("id") Integer formId,
        @FormParam("name") String name,
        @FormParam("description") String description,
        @FormParam("category_id") Integer categoryId,
        @FormParam("price") Double price
        ) {
        Category category = Category.findById(categoryId);
        if (category == null) {
            return new ProductResult(false, "Category for id " + categoryId +" was not found", null);
        }
        if (name == null) {
            return new ProductResult(false, "Product name must have a value", null);
        }
        if (id == null || !id.equals(formId)) {
            return new ProductResult(false, "Resource ID must be valid and match product id", null);
        }

        Product product = Product.findById(id);
        if (product == null) {
            return new ProductResult(false, "No product found", null);
        }

        product.name = name;
        product.description = description;
        product.category = category;
        product.price = price;
        product.modified = LocalDateTime.now();

        product.persist();
        return new ProductResult(true, "Product saved", product);
    }

    @DELETE
    @Path("{id}")
    @Authenticated
    @Operation(summary = "Delete product", description = "Delete a single product by ID")
    @Counted(name = "countDeleteProduct", description = "How many delete a product calls have been performed.")
    @Timed(name = "perfDeleteProduct", description = "A measure of how long it takes to delete a product.", unit = MetricUnits.MILLISECONDS)
    public Response delete(@PathParam Integer id) {
        Product product = Product.findById(id);
        if (product != null) {
            product.delete();
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

    public class ProductResult {
        public String message;
        public boolean success;
        public Product product;

        public ProductResult() {}

        public ProductResult(boolean success, String message, Product product) {
            this.success = success;
            this.message = message;
            this.product = product;
        }

    }
}