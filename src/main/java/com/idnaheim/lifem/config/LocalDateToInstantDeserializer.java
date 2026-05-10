package com.idnaheim.lifem.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class LocalDateToInstantDeserializer extends JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getText().trim();
        // If it already contains time info, parse as Instant directly
        if (value.contains("T")) {
            return Instant.parse(value);
        }
        // Otherwise treat as date-only and convert to start of day UTC
        LocalDate date = LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE);
        return date.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

}
