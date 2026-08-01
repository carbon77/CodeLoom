package com.codeloom.executor.engine

data class CompilationResult(
    val isSuccessful: Boolean,
    val stderr: String,
)
