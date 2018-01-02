package com.polidea.shuttle.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.polidea.shuttle.configuration.converters.PermissionConverter;
import com.polidea.shuttle.configuration.converters.PlatformConverter;
import com.polidea.shuttle.domain.app.Platform;
import com.polidea.shuttle.domain.user.permissions.PermissionType;
import com.polidea.shuttle.infrastructure.json.PermissionTypeDeserializer;
import com.polidea.shuttle.infrastructure.json.PermissionTypeSerializer;
import com.polidea.shuttle.infrastructure.json.PlatformDeserializer;
import com.polidea.shuttle.infrastructure.json.PlatformSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.HashSet;
import java.util.Set;

@Configuration
@SuppressWarnings("unused")
public class ConverterConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new PlatformConverter());
        registry.addConverter(new PermissionConverter());
    }

    @Bean
    public ConversionService conversionService() {
        ConversionServiceFactoryBean bean = new ConversionServiceFactoryBean();
        bean.setConverters(getConverters());
        bean.afterPropertiesSet();
        return bean.getObject();
    }

    private Set<Converter<?, ?>> getConverters() {
        Set<Converter<?, ?>> converters = new HashSet<>();
        converters.add(new PlatformConverter());
        converters.add(new PermissionConverter());
        return converters;
    }

    @Bean
    MappingJackson2HttpMessageConverter converter() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());
        return converter;
    }

    @Bean
    @Primary
    ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(createModule());
        return mapper;
    }

    private SimpleModule createModule() {
        SimpleModule module = new SimpleModule();

        module.addSerializer(Platform.class, new PlatformSerializer());
        module.addSerializer(PermissionType.class, new PermissionTypeSerializer());

        module.addDeserializer(Platform.class, new PlatformDeserializer());
        module.addDeserializer(PermissionType.class, new PermissionTypeDeserializer());

        return module;
    }
}
