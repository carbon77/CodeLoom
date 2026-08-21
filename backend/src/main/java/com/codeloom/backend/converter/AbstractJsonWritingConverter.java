package com.codeloom.backend.converter;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.postgresql.util.PGobject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.core.mapping.JdbcValue;
import tools.jackson.databind.ObjectMapper;

import java.sql.JDBCType;
import java.sql.SQLException;

@RequiredArgsConstructor
public abstract class AbstractJsonWritingConverter<T> implements Converter<T, JdbcValue> {
    private final ObjectMapper mapper;

    @Override
    public JdbcValue convert(@NonNull T source) {
        try {
            PGobject obj = new PGobject();
            obj.setType("jsonb");
            obj.setValue(mapper.writeValueAsString(source));
            return JdbcValue.of(obj, JDBCType.OTHER);
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
