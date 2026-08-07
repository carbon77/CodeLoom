package com.codeloom.backend.dto

import jakarta.validation.constraints.NotBlank

data class CreateTopicRequest(
    @NotBlank(message = "Can't create topic without name")
    val name: String
)