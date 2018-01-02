package com.polidea.shuttle.configuration.converters;

import com.polidea.shuttle.domain.user.permissions.PermissionType;
import org.springframework.core.convert.converter.Converter;

public class PermissionConverter implements Converter<String, PermissionType> {

    @Override
    public PermissionType convert(String source) {
        return PermissionType.determinePermissionType(source);
    }

}
