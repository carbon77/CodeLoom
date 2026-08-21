package com.codeloom.backend.service;

import static com.codeloom.backend.config.AuthenticationUtils.*;

import com.codeloom.backend.dao.SubmissionRepository;
import com.codeloom.backend.dao.problem.ProblemRepository;
import com.codeloom.backend.dao.testcase.TestCaseRepository;
import com.codeloom.backend.dto.SendSubmissionRequest;
import com.codeloom.backend.exception.*;
import com.codeloom.backend.model.*;
import com.codeloom.common.*;
import java.util.Collection;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class SubmissionService {
    private static final Logger log = LoggerFactory.getLogger(SubmissionService.class);
    private final SubmissionRepository repo;
    private final ProblemRepository problems;
    private final TestCaseRepository tests;
    private final KafkaTemplate<String, String> kafka;
    private final ObjectMapper mapper;
    private final String topic;

    public SubmissionService(
            SubmissionRepository r,
            ProblemRepository p,
            TestCaseRepository t,
            KafkaTemplate<String, String> k,
            ObjectMapper m,
            @Value("${codeloom.kafka.submission-topic}") String x) {
        repo = r;
        problems = p;
        tests = t;
        kafka = k;
        mapper = m;
        topic = x;
    }

    public Collection<Submission> findSubmissions(long id, Authentication a) {
        return repo.findByUserIdAndProblemId(getUserId(a), id);
    }

    @Transactional
    public void sendSubmission(SendSubmissionRequest q, Authentication a) {
        Problem p = problems.findById(q.problemId()).orElseThrow(() -> new ProblemNotFoundException(q.problemId()));
        if (isRegularUser(a) && !p.isPublished()) throw new ProblemNotFoundException(q.problemId());
        if (tests.countAllByProblemId(q.problemId()) == 0) throw new NoTestCasesException(q.problemId());
        Submission s = repo.save(
                new Submission(getUserId(a), q.problemId(), q.code(), SubmissionStatus.PENDING, q.language()));
        SubmissionEvent e = new SubmissionEvent(s.getId(), s.getUserId(), q.problemId(), q.code(), q.language());
        kafka.send(topic, s.getId().toString(), mapper.writeValueAsString(e));
        log.info("Submission sent: submissionId={}", s.getId());
    }
}
