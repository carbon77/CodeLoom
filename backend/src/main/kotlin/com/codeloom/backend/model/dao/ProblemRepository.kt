package com.codeloom.backend.model.dao

import com.codeloom.backend.model.Problem
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProblemRepository : CrudRepository<Problem, UUID>