package com.codeloom.executor.service

import com.codeloom.executor.dto.CodeExecutionRequest
import com.codeloom.executor.dto.CodeExecutionResult

interface CodeExecutorService {
    fun run(request: CodeExecutionRequest): CodeExecutionResult
}