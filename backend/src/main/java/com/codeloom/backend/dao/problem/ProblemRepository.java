package com.codeloom.backend.dao.problem;

import com.codeloom.backend.model.Problem;
import org.springframework.data.repository.CrudRepository;

public interface ProblemRepository extends CrudRepository<Problem, Long>, ProblemRepositoryCustom {
    Problem findBySlug(String slug);
}
