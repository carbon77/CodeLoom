package com.codeloom.backend.config;

import com.codeloom.backend.exception.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class OpenApiConfig {
    public static final String BAD_REQUEST_RESPONSE_REF = "#/components/responses/BadRequest";
    public static final String NOT_FOUND_RESPONSE_REF = "#/components/responses/NotFound";
    public static final String FORBIDDEN_RESPONSE_REF = "#/components/responses/Forbidden";

    private static final Map<String, String> ERRORS = Map.of(
            "BadRequest",
            "The request is invalid",
            "Unauthorized",
            "Authentication is required",
            "Forbidden",
            "The authenticated user does not have permission",
            "NotFound",
            "The requested resource was not found",
            "InternalServerError",
            "An unexpected server error occurred");
    private static final Map<String, String> COMMON =
            Map.of("401", "Unauthorized", "403", "Forbidden", "500", "InternalServerError");

    @Bean
    public OpenApiCustomizer errorResponseOpenApiCustomizer() {
        return api -> {
            Components components = api.getComponents() == null ? new Components() : api.getComponents();
            api.setComponents(components);

            ModelConverters.getInstance().read(ErrorResponse.class).forEach(components::addSchemas);
            ERRORS.forEach((error, description) -> components.addResponses(error, errorResponse(description)));

            if (api.getPaths() != null)
                api.getPaths().entrySet().stream()
                        .filter(entry -> entry.getKey().startsWith("/v1"))
                        .flatMap(entry -> entry.getValue().readOperations().stream())
                        .forEach(operation -> {
                            ApiResponses responses = operation.getResponses() == null ? new ApiResponses() : operation.getResponses();
                            operation.setResponses(responses);
                            COMMON.forEach((statusCode, name) -> {
                                if (!responses.containsKey(statusCode))
                                    responses.addApiResponse(statusCode, new ApiResponse().$ref("#/components/responses/" + name));
                            });
                        });
        };
    }

    private ApiResponse errorResponse(String description) {
        return new ApiResponse()
                .description(description)
                .content(new Content()
                        .addMediaType(
                                "application/json",
                                new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));
    }
}
