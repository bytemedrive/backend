package com.bytemedrive.backend.command.file.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotEmpty;


public record FileUpload(@JsonProperty("id") @NotEmpty String id, @JsonProperty("dataBase64") @NotEmpty String data) {

}
