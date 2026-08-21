package com.codeloom.backend.converter;

import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import tools.jackson.databind.ObjectMapper;

public abstract class AbstractJsonReadingConverter<T> implements Converter<PGobject, T> {
  private final ObjectMapper mapper;
  private final Class<T> type;

  protected AbstractJsonReadingConverter(ObjectMapper m, Class<T> t) {
    mapper = m;
    type = t;
  }

  public T convert(PGobject source) {
    return mapper.readValue(source.getValue(), type);
  }
}
