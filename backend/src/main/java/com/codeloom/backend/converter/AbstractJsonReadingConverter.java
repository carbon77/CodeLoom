package com.codeloom.backend.converter;

import lombok.RequiredArgsConstructor;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
public abstract class AbstractJsonReadingConverter<T> implements Converter<PGobject, T> {
    private final ObjectMapper mapper;
    private final Class<T> type;

    @Override
    public T convert(PGobject source) {
        return mapper.readValue(source.getValue(), type);
    }
}
