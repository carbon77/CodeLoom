package com.codeloom.backend.config;

import com.codeloom.backend.exception.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.*;
import java.util.Map;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {
  public static final String BAD_REQUEST_RESPONSE_REF = "#/components/responses/BadRequest",
      NOT_FOUND_RESPONSE_REF = "#/components/responses/NotFound",
      FORBIDDEN_RESPONSE_REF = "#/components/responses/Forbidden";
  private static final Map<String, String> ERRORS =
      Map.of(
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
  OpenApiCustomizer errorResponseOpenApiCustomizer() {
    return api -> {
      Components c = api.getComponents() == null ? new Components() : api.getComponents();
      api.setComponents(c);
      ModelConverters.getInstance().read(ErrorResponse.class).forEach(c::addSchemas);
      ERRORS.forEach((n, d) -> c.addResponses(n, errorResponse(d)));
      if (api.getPaths() != null)
        api.getPaths().entrySet().stream()
            .filter(e -> e.getKey().startsWith("/v1"))
            .flatMap(e -> e.getValue().readOperations().stream())
            .forEach(
                o -> {
                  ApiResponses r = o.getResponses() == null ? new ApiResponses() : o.getResponses();
                  o.setResponses(r);
                  COMMON.forEach(
                      (s, n) -> {
                        if (!r.containsKey(s))
                          r.addApiResponse(
                              s, new ApiResponse().$ref("#/components/responses/" + n));
                      });
                });
    };
  }

  private ApiResponse errorResponse(String d) {
    return new ApiResponse()
        .description(d)
        .content(
            new Content()
                .addMediaType(
                    "application/json",
                    new MediaType()
                        .schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))));
  }
}
