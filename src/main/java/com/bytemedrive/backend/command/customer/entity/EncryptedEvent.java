package com.bytemedrive.backend.command.customer.entity;

import com.fasterxml.jackson.annotation.JsonProperty;


public record EncryptedEvent(@JsonProperty("dataBase64") String dataBase64) {
}
