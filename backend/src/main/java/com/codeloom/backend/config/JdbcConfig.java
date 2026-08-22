package com.codeloom.backend.config;

import com.codeloom.backend.converter.*;
import com.codeloom.backend.model.*;
import com.codeloom.common.SubmissionStatus;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.postgresql.util.PGobject;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.*;
import org.springframework.data.jdbc.core.mapping.JdbcValue;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import tools.jackson.databind.ObjectMapper;

@Configuration
@RequiredArgsConstructor
public class JdbcConfig extends AbstractJdbcConfiguration {
    private final ObjectMapper objectMapper;

    @Override
    protected List<?> userConverters() {
        return List.of(
                new ExamplesRead(objectMapper),
                new ExamplesWrite(objectMapper),
                new ConstraintsRead(objectMapper),
                new ConstraintsWrite(objectMapper),
                new DifficultyWrite(),
                new StatusWrite());
    }

    @ReadingConverter
    static class ExamplesRead extends AbstractJsonReadingConverter<ProblemExamples> {
        ExamplesRead(ObjectMapper m) {
            super(m, ProblemExamples.class);
        }
    }

    @WritingConverter
    static class ExamplesWrite extends AbstractJsonWritingConverter<ProblemExamples> {
        ExamplesWrite(ObjectMapper m) {
            super(m);
        }
    }

    @ReadingConverter
    static class ConstraintsRead extends AbstractJsonReadingConverter<ProblemConstraints> {
        ConstraintsRead(ObjectMapper m) {
            super(m, ProblemConstraints.class);
        }
    }

    @WritingConverter
    static class ConstraintsWrite extends AbstractJsonWritingConverter<ProblemConstraints> {
        ConstraintsWrite(ObjectMapper m) {
            super(m);
        }
    }

    @WritingConverter
    static class DifficultyWrite
            implements org.springframework.core.convert.converter.Converter<ProblemDifficulty, JdbcValue> {
        public JdbcValue convert(ProblemDifficulty s) {
            return enumValue(s.name(), "problem_difficulty");
        }
    }

    @WritingConverter
    static class StatusWrite
            implements org.springframework.core.convert.converter.Converter<SubmissionStatus, JdbcValue> {
        public JdbcValue convert(SubmissionStatus s) {
            return JdbcValue.of(s.name(), JDBCType.VARCHAR);
        }
    }

    private static JdbcValue enumValue(String value, String type) {
        try {
            var object = new PGobject();
            object.setType(type);
            object.setValue(value);
            return JdbcValue.of(object, JDBCType.OTHER);
        } catch (SQLException exception) {
            throw new IllegalArgumentException(exception);
        }
    }
}
