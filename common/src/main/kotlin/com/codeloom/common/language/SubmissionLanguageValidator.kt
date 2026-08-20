package com.codeloom.common.language

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext

class SubmissionLanguageValidator : ConstraintValidator<ValidLanguage, String> {
    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?
    ): Boolean {
        if (value == null) {
            return false;
        }

        try {
            LanguageSpec.fromLanguage(value)
        } catch (_: InvalidLanguageException) {
            return false;
        }

        return true
    }
}