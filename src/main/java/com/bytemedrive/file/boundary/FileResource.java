package com.bytemedrive.file.boundary;

import com.bytemedrive.file.entity.FileUpload;
import software.amazon.awssdk.services.s3.S3Client;

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
    S3Client s3Client;

    @POST()
    public Response uploadFile(@NotNull @Valid FileUpload fileUpload) {
        return Response.ok().build();
    }
}