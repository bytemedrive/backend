package com.bytemedrive.backend.query.customer.boundary;


import javax.inject.Inject;
import javax.validation.constraints.NotEmpty;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;


@Path("/customers")
@Consumes("application/json")
@Produces("application/json")
public class CustomerResource {

    @Inject
    CustomerFacade facade;

    @GET
    @Path("/{customerIdHash}/events")
    public Response getEvents(@PathParam("customerIdHash") @NotEmpty String customerIdHash) {
        return Response.ok(facade.getCustomer(customerIdHash)).build();
    }
}
