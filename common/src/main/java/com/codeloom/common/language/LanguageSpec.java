package com.codeloom.common.language;

import jakarta.validation.constraints.NotBlank;
import org.jspecify.annotations.Nullable;

public record LanguageSpec(
        @NotBlank String name,
        @NotBlank String runImage,
        @NotBlank String compileImage,
        @NotBlank String sourceFile,
        @Nullable String compileCommand,
        @NotBlank String runCommand) {

    public LanguageSpec {
        compileImage = compileImage == null ? runImage : compileImage;
    }

    public String image(boolean compilation) {
        return compilation ? compileImage : runImage;
    }
}
