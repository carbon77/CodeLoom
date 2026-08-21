package com.codeloom.backend.converter;

import java.sql.*;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.core.mapping.JdbcValue;
import tools.jackson.databind.ObjectMapper;

public abstract class AbstractJsonWritingConverter<T> implements Converter<T, JdbcValue> {
  private final ObjectMapper mapper;

  protected AbstractJsonWritingConverter(ObjectMapper m, Class<T> t) {
    mapper = m;
  }

  public JdbcValue convert(T source) {
    try {
      PGobject o = new PGobject();
      o.setType("jsonb");
      o.setValue(mapper.writeValueAsString(source));
      return JdbcValue.of(o, JDBCType.OTHER);
    } catch (SQLException e) {
      throw new IllegalArgumentException(e);
    }
  }
}
