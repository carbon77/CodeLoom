package com.codeloom.common.language;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.Locale;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "codeloom")
public record LanguageProperties(@NotEmpty Map<String, @Valid LanguageSpec> languages) {

    public LanguageProperties {
        languages = languages == null ? Map.of() : Map.copyOf(languages);
    }

    public boolean supports(@Nullable String language) {
        return language != null && languages.containsKey(language.toLowerCase(Locale.ROOT));
    }

    public LanguageSpec require(String language) {
        var spec = languages.get(language.toLowerCase(Locale.ROOT));

        if (spec == null) {
            throw new InvalidLanguageException();
        }

        return spec;
    }
}
