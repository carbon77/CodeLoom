package com.codeloom.common.language

import jakarta.validation.Constraint
import jakarta.validation.Payload
import jakarta.validation.constraints.NotBlank
import kotlin.reflect.KClass

@NotBlank
@Constraint(validatedBy = [SubmissionLanguageValidator::class])
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class ValidLanguage(
    val message: String = "Invalid submission language",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)