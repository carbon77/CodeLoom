package com.codeloom.backend.dto;

import com.codeloom.common.language.ValidLanguage;
import jakarta.validation.constraints.*;
import lombok.Builder;

@Builder
public record SendSubmissionRequest(
        @NotNull Long problemId,
        @NotBlank String code,
        @ValidLanguage String language) {}
