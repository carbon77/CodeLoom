package com.codeloom.backend.service;

import static com.codeloom.backend.PatchValues.value;

import com.codeloom.backend.dao.testcase.TestCaseRepository;
import com.codeloom.backend.dto.CreateTestCaseRequest;
import com.codeloom.backend.model.TestCase;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.*;

@Service
public class TestCaseService {
  private final TestCaseRepository repo;
  private final ObjectMapper mapper;

  public TestCaseService(TestCaseRepository r, ObjectMapper m) {
    repo = r;
    mapper = m;
  }

  public TestCase findById(UUID id) {
    return repo.findById(id)
        .orElseThrow(
            () ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Entity with id `" + id + "` not found"));
  }

  public Iterable<TestCase> findAllByIds(List<UUID> ids) {
    return repo.findAllById(ids);
  }

  @Transactional
  public TestCase create(CreateTestCaseRequest q) {
    return repo.save(new TestCase(q.problemId(), q.input(), q.expectedOutput(), q.isPublic()));
  }

  @Transactional
  public TestCase patch(UUID id, JsonNode n) {
    TestCase t = findById(id);
    return repo.save(
        t.updated(
            value(n, "input", mapper, t.getInput(), String.class),
            value(n, "expectedOutput", mapper, t.getExpectedOutput(), String.class),
            value(n, "isPublic", mapper, t.isPublic(), Boolean.class)));
  }

  @Transactional
  public void delete(UUID id) {
    repo.deleteById(id);
  }

  public Iterable<TestCase> findAllByProblemId(long id, Boolean p) {
    return repo.findAllByProblemId(id, p);
  }
}
