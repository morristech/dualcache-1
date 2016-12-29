package com.iagocanalejas;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iagocanalejas.cache.interfaces.CacheSerializer;

import java.io.IOException;

/**
 * Created by Canalejas on 27/12/2016.
 */

public class JsonSerializer<T> implements CacheSerializer<T> {
    private final ObjectMapper mapper;
    private final Class<T> clazz;

    /**
     * Default constructor.
     *
     * @param clazz is the class of object to serialize/deserialize.
     */
    public JsonSerializer(Class<T> clazz) {
        this.clazz = clazz;
        mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    @Override
    public T fromString(String data) {
        try {
            return mapper.readValue(data, clazz);
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException();
    }

    @Override
    public String toString(T object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        throw new IllegalStateException();
    }
}