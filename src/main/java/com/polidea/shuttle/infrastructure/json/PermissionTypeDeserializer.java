package com.polidea.shuttle.infrastructure.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.polidea.shuttle.domain.user.permissions.PermissionType;

import java.io.IOException;

import static com.polidea.shuttle.domain.user.permissions.PermissionType.determinePermissionType;

public class PermissionTypeDeserializer extends JsonDeserializer<PermissionType> {
    @Override
    public PermissionType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return determinePermissionType(p.getValueAsString());
    }
}
