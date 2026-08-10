package com.codeloom.backend.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.Paths
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class OpenApiConfigTest {
    @Test
    fun `customizer registers error contract and applies common responses to v1 operations`() {
        val apiOperation = Operation()
        val actuatorOperation = Operation()
        val openApi =
            OpenAPI()
                .components(Components())
                .paths(
                    Paths()
                        .addPathItem("/v1/problems", PathItem().get(apiOperation))
                        .addPathItem("/actuator/health", PathItem().get(actuatorOperation)),
                )

        OpenApiConfig().errorResponseOpenApiCustomizer().customise(openApi)

        assertNotNull(openApi.components.schemas["ErrorResponse"])
        assertEquals(
            "#/components/schemas/ErrorResponse",
            openApi.components.responses["Unauthorized"].content["application/json"].schema.`$ref`,
        )
        assertTrue(apiOperation.responses.keys.containsAll(listOf("401", "403", "500")))
        assertEquals("#/components/responses/Unauthorized", apiOperation.responses["401"].`$ref`)
        assertFalse(actuatorOperation.responses?.containsKey("401") ?: false)
    }
}
