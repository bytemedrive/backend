package com.bytemedrive.backend.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vladmihalcea.hibernate.type.util.ObjectMapperSupplier;


/**
 * Customization for hibernate-types-52 which is used for DB JSON serialization. Mainly customized due to ZonedDateTime
 * serialization. See https://vladmihalcea.com/hibernate-types-customize-jackson-objectmapper/
 */
public class DatabaseConfig implements ObjectMapperSupplier {

    ObjectMapper objectMapper;

    public DatabaseConfig() {
        objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    }

    @Override
    public ObjectMapper get() {
        return objectMapper;
    }
}
