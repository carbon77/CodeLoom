package com.codeloom.backend.dao.problem

import com.codeloom.backend.dto.ProblemFilters
import com.codeloom.backend.dto.ProblemListDto

interface ProblemRepositoryCustom {
    fun findProblemListDtos(filters: ProblemFilters): List<ProblemListDto>
}
