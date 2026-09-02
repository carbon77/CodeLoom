package com.codeloom.backend.controller;

import com.codeloom.backend.dto.LanguageDisplayDto;
import com.codeloom.backend.dto.LanguageSystemDto;
import com.codeloom.common.language.LanguageProperties;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/languages")
@RequiredArgsConstructor
@Tag(name = "Languages", description = "Supported submission languages and execution commands")
public class LanguageController {
    private final LanguageProperties languageProperties;

    @GetMapping("/display")
    @Operation(
            operationId = "listLanguageDisplayInformation",
            summary = "List available languages",
            description = "Returns the stable key and display name of every configured language.")
    @ApiResponse(
            responseCode = "200",
            description = "Language display information returned",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = LanguageDisplayDto.class))))
    public List<LanguageDisplayDto> displayInformation() {
        return languageProperties.languages().entrySet().stream()
                .map(entry ->
                        new LanguageDisplayDto(entry.getKey(), entry.getValue().name()))
                .sorted(Comparator.comparing(LanguageDisplayDto::name))
                .toList();
    }

    @GetMapping("/system")
    @Operation(
            operationId = "listLanguageSystemInformation",
            summary = "List language commands",
            description = "Returns compile and run commands without exposing container images.")
    @ApiResponse(
            responseCode = "200",
            description = "Language command information returned",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = LanguageSystemDto.class))))
    public List<LanguageSystemDto> systemInformation() {
        return languageProperties.languages().entrySet().stream()
                .map(entry -> new LanguageSystemDto(
                        entry.getKey(),
                        entry.getValue().compileCommand(),
                        entry.getValue().runCommand()))
                .sorted(Comparator.comparing(
                        dto -> languageProperties.require(dto.key()).name()))
                .toList();
    }
}
