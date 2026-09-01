package com.codeloom.backend.dao.submission;

import static com.codeloom.backend.jooq.Tables.SUBMISSIONS;

import com.codeloom.backend.dto.SubmissionListDto;
import java.util.Collection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SubmissionRepositoryCustomImpl implements SubmissionRepositoryCustom {
    private final DSLContext dsl;

    @Override
    public Collection<SubmissionListDto> findListDtos(UUID userId, long problemId) {
        return dsl.select(
                        SUBMISSIONS.SUBMISSION_ID,
                        SUBMISSIONS.STATE,
                        SUBMISSIONS.LANGUAGE,
                        SUBMISSIONS.CREATED_AT.as("createdAt"))
                .from(SUBMISSIONS)
                .where(SUBMISSIONS.USER_ID.eq(userId), SUBMISSIONS.PROBLEM_ID.eq(Math.toIntExact(problemId)))
                .fetchInto(SubmissionListDto.class);
    }
}
