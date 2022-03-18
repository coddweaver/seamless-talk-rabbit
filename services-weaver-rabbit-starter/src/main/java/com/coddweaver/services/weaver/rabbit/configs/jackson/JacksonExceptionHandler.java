package com.coddweaver.services.weaver.rabbit.configs.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.converter.MessageConversionException;

@Slf4j
public class JacksonExceptionHandler extends DeserializationProblemHandler {

    @Override
    public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p,
            JsonDeserializer<?> deserializer, Object beanOrClass, String propertyName) {
        if (ctxt.hasDeserializationFeatures(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES.getMask())) {
            logAndThrow("JSON conversion error! Unknown property: " + propertyName);
        }
        return true;
    }

    private void logAndThrow(String error) {
        log.error(error);
        throw new MessageConversionException(error);
    }
}