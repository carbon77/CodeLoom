package com.codeloom.common.language;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SubmissionLanguageValidator implements ConstraintValidator<ValidLanguage, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;
        try {
            LanguageSpec.fromLanguage(value);
            return true;
        } catch (InvalidLanguageException ignored) {
            return false;
        }
    }
}
