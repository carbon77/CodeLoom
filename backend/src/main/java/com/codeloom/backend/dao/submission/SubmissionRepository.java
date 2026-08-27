package com.codeloom.backend.dao.submission;

import com.codeloom.backend.model.Submission;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface SubmissionRepository extends CrudRepository<Submission, UUID>, SubmissionRepositoryCustom {}
