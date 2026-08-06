package com.codeloom.backend.dao.problem

import com.codeloom.backend.dto.ProblemDto
import com.codeloom.backend.dto.ProblemFilters
import com.codeloom.backend.dto.ProblemListDto
import com.codeloom.backend.model.Problem

interface ProblemRepositoryCustom {
    fun findProblemListDtos(filters: ProblemFilters): List<ProblemListDto>
    fun findProblemDtoBySlug(slug: String): ProblemDto?
}