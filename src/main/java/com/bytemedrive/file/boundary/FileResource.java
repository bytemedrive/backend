package com.bytemedrive.file.boundary;

import com.bytemedrive.file.entity.FileUpload;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;


@Path("/files")
public class FileResource {

    @POST()
    @Path("/upload")
    public Response uploadFile(@NotNull @Valid FileUpload fileUpload) {
        return Response.ok().build();
    }
}