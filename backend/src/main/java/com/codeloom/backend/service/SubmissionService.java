package com.codeloom.backend.service;

import static com.codeloom.backend.security.AuthenticationUtils.getUserId;
import static com.codeloom.backend.security.AuthenticationUtils.isRegularUser;

import com.codeloom.backend.dao.problem.ProblemRepository;
import com.codeloom.backend.dao.submission.SubmissionRepository;
import com.codeloom.backend.dao.testcase.TestCaseRepository;
import com.codeloom.backend.dao.testcase.TestCaseResultRepository;
import com.codeloom.backend.dto.SendSubmissionRequest;
import com.codeloom.backend.dto.SubmissionDto;
import com.codeloom.backend.dto.SubmissionListDto;
import com.codeloom.backend.dto.SubmissionStateDto;
import com.codeloom.backend.dto.TestCaseResultListDto;
import com.codeloom.backend.exception.NoTestCasesException;
import com.codeloom.backend.exception.ProblemNotFoundException;
import com.codeloom.backend.exception.SubmissionNotFoundException;
import com.codeloom.backend.model.Problem;
import com.codeloom.backend.model.Submission;
import com.codeloom.common.SubmissionKafkaEvent;
import com.codeloom.common.SubmissionState;
import java.util.Collection;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final TestCaseResultRepository testCaseResultRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${codeloom.kafka.topics.submission}")
    private String topic;

    public Collection<SubmissionListDto> findSubmissions(long problemId, Authentication authentication) {
        return submissionRepository.findListDtos(getUserId(authentication), problemId);
    }

    @Transactional
    public SubmissionStateDto sendSubmission(SendSubmissionRequest request, Authentication authentication) {
        Problem problem = problemRepository
                .findById(request.problemId())
                .orElseThrow(() -> new ProblemNotFoundException(request.problemId()));

        if (isRegularUser(authentication) && problem.isDraft()) {
            throw new ProblemNotFoundException(request.problemId());
        }

        if (testCaseRepository.countAllByProblemId(request.problemId()) == 0) {
            throw new NoTestCasesException(request.problemId());
        }

        Submission submission = submissionRepository.save(Submission.builder()
                .userId(getUserId(authentication))
                .problemId(request.problemId())
                .code(request.code())
                .state(SubmissionState.PENDING)
                .language(request.language())
                .build());
        SubmissionKafkaEvent event = SubmissionKafkaEvent.builder()
                .submissionId(submission.getId())
                .userId(submission.getUserId())
                .problemId(request.problemId())
                .code(request.code())
                .language(request.language())
                .executionTimeLimitMs(
                        problem.getConstraints() == null
                                ? null
                                : problem.getConstraints().executionTimeLimitMs())
                .memoryUsageLimitMb(
                        problem.getConstraints() == null
                                ? null
                                : problem.getConstraints().memoryUsageLimitMb())
                .build();

        kafkaTemplate.send(topic, submission.getId().toString(), objectMapper.writeValueAsString(event));
        log.info("Submission sent: submissionId={}", submission.getId());
        return SubmissionStateDto.builder()
                .submissionId(submission.getId())
                .state(submission.getState())
                .build();
    }

    public SubmissionDto findSubmissionDetails(Authentication authentication, UUID submissionId) {
        Submission submission = submissionRepository
                .findById(submissionId)
                .orElseThrow(() -> new SubmissionNotFoundException(submissionId));

        if (!getUserId(authentication).equals(submission.getUserId())) {
            throw new SubmissionNotFoundException(submissionId);
        }

        return SubmissionDto.builder()
                .submissionId(submission.getId())
                .state(submission.getState())
                .language(submission.getLanguage())
                .code(submission.getCode())
                .errorMessage(submission.getErrorMessage())
                .createdAt(submission.getCreatedAt())
                .results(testCaseResultRepository.findAllBySubmissionId(submissionId).stream()
                        .map(TestCaseResultListDto::fromEntity)
                        .toList())
                .build();
    }
}
