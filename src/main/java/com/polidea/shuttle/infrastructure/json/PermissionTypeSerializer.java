package com.polidea.shuttle.infrastructure.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.polidea.shuttle.domain.user.permissions.PermissionType;

import java.io.IOException;

public class PermissionTypeSerializer extends JsonSerializer<PermissionType> {
    @Override
    public void serialize(PermissionType permissionType, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(permissionType.name);
    }
}
