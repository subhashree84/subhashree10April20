package com.ims.config;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class JsonDateTimeSerializer extends JsonSerializer<LocalDateTime> {
    @Override
    public void serialize(LocalDateTime date, JsonGenerator generator, SerializerProvider arg) throws IOException,
            JsonProcessingException {
        final String dateString = date.format(DateTimeFormatter.ofPattern("yyyy MMM dd HH:mm:ss z"));
        System.out.println("Serializer -------------- dateString ---------------------------------->"+dateString);
        generator.writeString(dateString);
    }
}