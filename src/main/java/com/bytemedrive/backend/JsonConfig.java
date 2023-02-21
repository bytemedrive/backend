package com.bytemedrive.backend;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.quarkus.jackson.ObjectMapperCustomizer;

import javax.inject.Singleton;
import java.io.IOException;


@Singleton
public class JsonConfig implements ObjectMapperCustomizer {

    public void customize(ObjectMapper mapper) {
        try {
            mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                    .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                    // com.stripe.model.PaymentIntent has gson JsonObject attribute which should not be serialized
                    .addMixIn(Class.forName("com.google.gson.JsonObject"), MyMixInForIgnoreType.class);
            // when a map contains null value as key jackson does not have default serializer for it and causes
            // com.fasterxml.jackson.databind.JsonMappingException: Null key for a Map not allowed in JSON (use a converting NullKeySerializer?)
            mapper.getSerializerProvider().setNullKeySerializer(new NullKeySerializer());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @JsonIgnoreType
    public class MyMixInForIgnoreType {
    }

    public static class NullKeySerializer extends StdSerializer<Object> {

        protected NullKeySerializer() {
            super((Class<Object>) null);
        }

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeFieldName("");
        }
    }
}
