package com.bytemedrive.backend.command.customer.boundary;


import com.bytemedrive.backend.command.customer.entity.EncryptedEvent;
import com.bytemedrive.backend.command.customer.entity.EncryptedEventPublished;
import com.bytemedrive.backend.store.root.boundary.StoreFacade;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;


@Path("/customers")
@Consumes("application/json")
@Produces("application/json")
public class CustomerResource {

    @Inject
    StoreFacade storeFacade;

    @POST
    @Path("/{idHash}/events")
    public void publishEvent(@PathParam("idHash") String idHash, EncryptedEvent event) {
        storeFacade.publishEvent(new EncryptedEventPublished(idHash, event.dataBase64()));
    }
}
