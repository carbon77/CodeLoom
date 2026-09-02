package com.codeloom.common.language;

import java.util.Locale;
import java.util.Map;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "codeloom")
public record LanguageProperties(Map<String, LanguageSpec> languages) {

    public boolean supports(@Nullable String language) {
        return language != null && languages.containsKey(language);
    }

    public LanguageSpec require(String language) {
        var spec = languages.get(language.toLowerCase(Locale.ROOT));

        if (spec == null) {
            throw new InvalidLanguageException();
        }

        return spec;
    }
}
