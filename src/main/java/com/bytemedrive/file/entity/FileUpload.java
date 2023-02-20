package com.bytemedrive.file.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotEmpty;


public record FileUpload(@JsonProperty("id") @NotEmpty String id, @JsonProperty("file") @NotEmpty String file) {

}
