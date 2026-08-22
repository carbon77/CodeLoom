package com.codeloom.backend.service;

import com.codeloom.backend.dao.testcase.TestCaseRepository;
import com.codeloom.backend.dto.CreateTestCaseRequest;
import com.codeloom.backend.dto.UpdateTestCaseRequest;
import com.codeloom.backend.model.TestCase;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@RequiredArgsConstructor
@Service
public class TestCaseService {
    private final TestCaseRepository testCaseRepository;

    public TestCase findById(UUID testCaseId) {
        return testCaseRepository
                .findById(testCaseId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Entity with id `" + testCaseId + "` not found"));
    }

    public Iterable<TestCase> findAllByIds(List<UUID> ids) {
        return testCaseRepository.findAllById(ids);
    }

    @Transactional
    public TestCase create(CreateTestCaseRequest request) {
        return testCaseRepository.save(TestCase.builder()
                .problemId(request.problemId())
                .input(request.input())
                .expectedOutput(request.expectedOutput())
                .isPublic(request.isPublic())
                .build());
    }

    @Transactional
    public TestCase update(UUID testCaseId, UpdateTestCaseRequest request) {
        TestCase testCase = findById(testCaseId)
                .withProblemId(request.problemId())
                .withExpectedOutput(request.expectedOutput())
                .withInput(request.input())
                .withPublic(request.isPublic());
        return testCaseRepository.save(testCase);
    }

    @Transactional
    public void delete(UUID testCaseId) {
        testCaseRepository.deleteById(testCaseId);
    }

    public Iterable<TestCase> findAllByProblemId(long problemId, Boolean isPublic) {
        return testCaseRepository.findAllByProblemId(problemId, isPublic);
    }
}
