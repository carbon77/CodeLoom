package com.codeloom.backend.model

data class ProblemExample(
    val input: String,
    val output: String,
    val explanation: String? = null,
)
