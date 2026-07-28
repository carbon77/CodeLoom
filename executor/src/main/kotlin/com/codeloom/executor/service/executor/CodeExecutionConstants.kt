package com.codeloom.executor.service.executor

const val ERROR_EXIT_CODE = 1L

const val TIMEOUT_EXIT_CODE = 124L
const val TIMEOUT_MESSAGE = "Execution timed out"
const val DEFAULT_TIMEOUT_MS = 5000L

const val MEMORY_LIMIT_EXCEEDED_EXIT_CODE = 137L
const val MEMORY_LIMIT_EXCEEDED_MESSAGE = "Memory limit exceeded"
const val DEFAULT_MEMORY_LIMIT_BYTES = 256L * 1024L * 1024L // 256 MB
