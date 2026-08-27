package com.codeloom.backend.dao.submission;

import com.codeloom.backend.model.Submission;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface SubmissionRepository extends CrudRepository<Submission, UUID>, SubmissionRepositoryCustom {
}
