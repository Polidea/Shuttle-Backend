package com.polidea.shuttle.configuration.converters;

import com.polidea.shuttle.domain.app.Platform;
import org.springframework.core.convert.converter.Converter;

public class PlatformConverter implements Converter<String, Platform> {

    @Override
    public Platform convert(String value) {
        return Platform.determinePlatformFromText(value);
    }

}
