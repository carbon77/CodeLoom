package com.codeloom.backend.config

import com.codeloom.backend.exception.ErrorResponse
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE

const val BAD_REQUEST_RESPONSE_REF = "#/components/responses/BadRequest"
const val NOT_FOUND_RESPONSE_REF = "#/components/responses/NotFound"

@Configuration
class OpenApiConfig {
    @Bean
    fun errorResponseOpenApiCustomizer(): OpenApiCustomizer =
        OpenApiCustomizer { openApi ->
            registerErrorResponseSchema(openApi)
            registerErrorResponses(openApi)
            addCommonErrorResponses(openApi)
        }

    private fun registerErrorResponseSchema(openApi: OpenAPI) {
        val components = openApi.components ?: Components().also { openApi.components = it }
        ModelConverters.getInstance().read(ErrorResponse::class.java).forEach(components::addSchemas)
    }

    private fun registerErrorResponses(openApi: OpenAPI) {
        val components = openApi.components ?: Components().also { openApi.components = it }
        ERROR_RESPONSES.forEach { (name, definition) ->
            components.addResponses(name, errorResponse(definition.description))
        }
    }

    private fun addCommonErrorResponses(openApi: OpenAPI) {
        openApi.paths
            ?.filterKeys { it.startsWith("/v1") }
            ?.values
            ?.flatMap { it.readOperations() }
            ?.forEach { operation ->
                val responses = operation.responses ?: ApiResponses().also { operation.responses = it }
                COMMON_ERROR_RESPONSES.forEach { (status, name) ->
                    if (!responses.containsKey(status)) {
                        responses.addApiResponse(status, ApiResponse().`$ref`("#/components/responses/$name"))
                    }
                }
            }
    }

    private fun errorResponse(description: String): ApiResponse =
        ApiResponse()
            .description(description)
            .content(
                Content().addMediaType(
                    APPLICATION_JSON_VALUE,
                    MediaType().schema(Schema<Any>().`$ref`("#/components/schemas/ErrorResponse")),
                ),
            )

    private data class ErrorResponseDefinition(val description: String)

    private companion object {
        val ERROR_RESPONSES =
            mapOf(
                "BadRequest" to ErrorResponseDefinition("The request is invalid"),
                "Unauthorized" to ErrorResponseDefinition("Authentication is required"),
                "Forbidden" to ErrorResponseDefinition("The authenticated user does not have permission"),
                "NotFound" to ErrorResponseDefinition("The requested resource was not found"),
                "InternalServerError" to ErrorResponseDefinition("An unexpected server error occurred"),
            )

        val COMMON_ERROR_RESPONSES =
            mapOf(
                "401" to "Unauthorized",
                "403" to "Forbidden",
                "500" to "InternalServerError",
            )
    }
}
