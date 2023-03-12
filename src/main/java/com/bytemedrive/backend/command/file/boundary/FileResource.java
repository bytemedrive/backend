package com.bytemedrive.backend.command.file.boundary;

import com.bytemedrive.backend.command.file.entity.EventFileUploaded;
import com.bytemedrive.backend.command.file.entity.FileUpload;
import com.bytemedrive.backend.store.root.boundary.StoreFacade;
import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@Path("/files")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FileResource {

    @Inject
    Logger log;

    @Inject
    StoreFacade storeFacade;

    @POST()
    public Response uploadFile(@NotNull @Valid FileUpload fileUpload) {
        storeFacade.publishEvent(new EventFileUploaded(fileUpload.id()));

        log.infof("Uploaded file: " + fileUpload);
        return Response.accepted().build();
    }
}