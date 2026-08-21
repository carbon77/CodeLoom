package com.codeloom.backend.dao;

import com.codeloom.backend.model.Submission;
import java.util.*;
import org.springframework.data.repository.CrudRepository;

public interface SubmissionRepository extends CrudRepository<Submission, UUID> {
  Collection<Submission> findByUserIdAndProblemId(UUID userId, long problemId);
}
