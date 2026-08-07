package com.codeloom.backend.dao.problem

import com.codeloom.backend.model.Problem
import org.springframework.data.repository.CrudRepository

interface ProblemRepository : CrudRepository<Problem, Long>, ProblemRepositoryCustom {
}