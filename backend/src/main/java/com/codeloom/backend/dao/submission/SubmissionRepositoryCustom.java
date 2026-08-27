package com.codeloom.backend.dao.submission;

import com.codeloom.backend.dto.SubmissionListDto;

import java.util.Collection;
import java.util.UUID;

public interface SubmissionRepositoryCustom {
    Collection<SubmissionListDto> findListDtos(UUID userId, long problemId);
}
