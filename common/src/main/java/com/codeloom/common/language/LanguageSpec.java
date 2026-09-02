package com.codeloom.common.language;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

public record LanguageSpec(
        @NotBlank String image,
        @NotBlank String sourceFile,
        @Nullable String compileCommand,
        @NotBlank String runCommand) {}
