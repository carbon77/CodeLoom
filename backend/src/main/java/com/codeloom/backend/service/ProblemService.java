package com.codeloom.backend.service;

import static com.codeloom.backend.config.AuthenticationUtils.isRegularUser;

import com.codeloom.backend.dao.problem.ProblemRepository;
import com.codeloom.backend.dao.testcase.TestCaseRepository;
import com.codeloom.backend.dto.*;
import com.codeloom.backend.exception.*;
import com.codeloom.backend.model.Problem;
import com.codeloom.backend.transformer.ProblemTransformer;
import java.time.Instant;
import java.util.List;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;

@Service
public class ProblemService {
  private final ProblemRepository repo;
  private final ProblemTransformer transformer;
  private final TestCaseRepository tests;
  private final TopicService topics;

  public ProblemService(
      ProblemRepository r, ProblemTransformer t, TestCaseRepository c, TopicService s) {
    repo = r;
    transformer = t;
    tests = c;
    topics = s;
  }

  @Transactional(readOnly = true)
  public List<ProblemListDto> findItemsByFilters(Authentication a, ProblemFilters f) {
    if (isRegularUser(a) && !f.publishedOnly()) throw new ForbiddenActionException();
    return repo.findProblemListDtos(f);
  }

  @Transactional(readOnly = true)
  public ProblemDto findDtoBySlug(Authentication a, String slug) {
    Problem p = repo.findBySlug(slug);
    if (p == null)
      throw new ResponseStatusException(
          HttpStatus.NOT_FOUND, "Problem with slug " + slug + " not found");
    if (isRegularUser(a) && !p.isPublished()) throw new ProblemNotFoundException(p.getId());
    return transformer.getDtoFromEntity(p);
  }

  @Transactional(readOnly = true)
  public Problem findById(Authentication a, long id) {
    Problem p = find(id);
    if (isRegularUser(a) && !p.isPublished()) throw new ProblemNotFoundException(id);
    return p;
  }

  @Transactional
  public void deleteById(long id) {
    repo.deleteById(id);
  }

  @Transactional
  public Problem create(CreateProblemRequest q) {
    try {
      return repo.save(new Problem(q.title(), q.title().toLowerCase().replace(" ", "_")));
    } catch (DuplicateKeyException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem already exists");
    }
  }

  @Transactional
  public Problem update(long id, JsonNode n) {
    Problem p = transformer.updateEntityFromPatchNode(find(id), n);
    if (n.has("topics")) topics.createManyWithProblem(id, n.get("topics"));
    try {
      return repo.save(p);
    } catch (DuplicateKeyException e) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem already exists");
    }
  }

  @Transactional
  public void publish(long id) {
    if (tests.countAllByProblemId(id) == 0) throw new NoTestCasesException(id);
    repo.save(find(id).withPublishedAt(Instant.now()));
  }

  @Transactional
  public void unpublish(long id) {
    repo.save(find(id).withPublishedAt(null));
  }

  private Problem find(long id) {
    return repo.findById(id).orElseThrow(() -> new ProblemNotFoundException(id));
  }
}
