package com.codeloom.backend.model

enum class SubmissionStatus {
    PENDING,
    RUNNING,
    ACCEPTED,
    COMPILATION_ERROR,
    RUNTIME_ERROR,
    TIME_LIMIT_EXCEEDED,
    MEMORY_LIMIT_EXCEEDED,
    WRONG_ANSWER,
}