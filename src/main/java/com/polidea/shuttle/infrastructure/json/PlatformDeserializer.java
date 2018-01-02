package com.polidea.shuttle.infrastructure.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.polidea.shuttle.domain.app.Platform;

import java.io.IOException;

public class PlatformDeserializer extends JsonDeserializer<Platform> {

    @Override
    public Platform deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        return Platform.determinePlatformFromText(jsonParser.getValueAsString());
    }

}
