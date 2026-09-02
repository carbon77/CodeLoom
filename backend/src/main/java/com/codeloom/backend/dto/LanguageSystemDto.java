package com.codeloom.backend.dto;

import org.jspecify.annotations.Nullable;

public record LanguageSystemDto(String key, @Nullable String compileCommand, String runCommand) {}
