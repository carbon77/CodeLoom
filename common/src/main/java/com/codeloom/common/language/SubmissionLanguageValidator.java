package com.codeloom.common.language;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SubmissionLanguageValidator implements ConstraintValidator<ValidLanguage, String> {
    private final LanguageProperties languageProperties;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return languageProperties.supports(value);
    }
}
