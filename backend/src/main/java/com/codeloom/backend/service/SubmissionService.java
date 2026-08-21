package com.codeloom.backend.service;

import com.codeloom.backend.dao.SubmissionRepository;
import com.codeloom.backend.dao.problem.ProblemRepository;
import com.codeloom.backend.dao.testcase.TestCaseRepository;
import com.codeloom.backend.dto.SendSubmissionRequest;
import com.codeloom.backend.exception.NoTestCasesException;
import com.codeloom.backend.exception.ProblemNotFoundException;
import com.codeloom.backend.model.Problem;
import com.codeloom.backend.model.Submission;
import com.codeloom.common.SubmissionEvent;
import com.codeloom.common.SubmissionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.Collection;

import static com.codeloom.backend.security.AuthenticationUtils.getUserId;
import static com.codeloom.backend.security.AuthenticationUtils.isRegularUser;

@Slf4j
@RequiredArgsConstructor
@Service
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${codeloom.kafka.submission-topic}")
    private final String topic;

    public Collection<Submission> findSubmissions(long problemId, Authentication authentication) {
        return submissionRepository.findByUserIdAndProblemId(getUserId(authentication), problemId);
    }

    @Transactional
    public void sendSubmission(SendSubmissionRequest request, Authentication authentication) {
        Problem problem = problemRepository
                .findById(request.problemId())
                .orElseThrow(() -> new ProblemNotFoundException(request.problemId()));

        if (isRegularUser(authentication) && problem.isDraft()) {
            throw new ProblemNotFoundException(request.problemId());
        }

        if (testCaseRepository.countAllByProblemId(request.problemId()) == 0) {
            throw new NoTestCasesException(request.problemId());
        }

        Submission submission = submissionRepository.save(
                Submission.builder()
                        .userId(getUserId(authentication))
                        .problemId(request.problemId())
                        .code(request.code())
                        .status(SubmissionStatus.PENDING)
                        .language(request.language())
                        .build()
        );
        SubmissionEvent event = SubmissionEvent.builder()
                .submissionId(submission.getId())
                .userId(submission.getUserId())
                .problemId(request.problemId())
                .code(request.code())
                .language(request.language())
                .build();

        kafkaTemplate.send(topic, submission.getId().toString(), objectMapper.writeValueAsString(event));
        log.info("Submission sent: submissionId={}", submission.getId());
    }
}
