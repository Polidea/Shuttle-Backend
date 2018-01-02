package com.polidea.shuttle.infrastructure.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.polidea.shuttle.domain.app.Platform;

import java.io.IOException;

public class PlatformSerializer extends JsonSerializer<Platform> {
    @Override
    public void serialize(Platform value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.name().toLowerCase());
    }
}
