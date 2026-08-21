package com.codeloom.backend.service;

import com.codeloom.backend.dao.problem.ProblemRepository;
import com.codeloom.backend.dao.testcase.TestCaseRepository;
import com.codeloom.backend.dto.CreateProblemRequest;
import com.codeloom.backend.dto.ProblemDto;
import com.codeloom.backend.dto.ProblemFilters;
import com.codeloom.backend.dto.ProblemListDto;
import com.codeloom.backend.exception.ForbiddenActionException;
import com.codeloom.backend.exception.NoTestCasesException;
import com.codeloom.backend.exception.ProblemNotFoundException;
import com.codeloom.backend.model.Problem;
import com.codeloom.backend.transformer.ProblemTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;

import java.time.Instant;
import java.util.List;

import static com.codeloom.backend.security.AuthenticationUtils.isRegularUser;

@Service
@RequiredArgsConstructor
public class ProblemService {
    private final ProblemRepository problemRepository;
    private final ProblemTransformer problemTransformer;
    private final TestCaseRepository testCaseRepository;
    private final TopicService topicService;

    @Transactional(readOnly = true)
    public List<ProblemListDto> findItemsByFilters(Authentication authentication, ProblemFilters filters) {
        if (isRegularUser(authentication) && !filters.publishedOnly()) {
            throw new ForbiddenActionException();
        }
        return problemRepository.findProblemListDtos(filters);
    }

    @Transactional(readOnly = true)
    public ProblemDto findDtoBySlug(Authentication authentication, String slug) {
        Problem problem = problemRepository.findBySlug(slug);
        if (problem == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Problem with slug " + slug + " not found");
        }

        if (isRegularUser(authentication) && problem.isDraft()) {
            throw new ProblemNotFoundException(problem.getId());
        }

        return problemTransformer.getDtoFromEntity(problem);
    }

    @Transactional(readOnly = true)
    public Problem findById(Authentication authentication, long problemId) {
        Problem problem = findOrThrow(problemId);

        if (isRegularUser(authentication) && problem.isDraft()) {
            throw new ProblemNotFoundException(problemId);
        }

        return problem;
    }

    @Transactional
    public void deleteById(long problemId) {
        problemRepository.deleteById(problemId);
    }

    @Transactional
    public Problem create(CreateProblemRequest request) {
        try {
            String slug = request.title().toLowerCase().replace(" ", "_");
            return problemRepository.save(
                    Problem.builder()
                            .title(request.title())
                            .slug(slug)
                            .build()
            );
        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem already exists");
        }
    }

    @Transactional
    public Problem update(long problemId, JsonNode patchNode) {
        Problem problem = problemTransformer.updateEntityFromPatchNode(findOrThrow(problemId), patchNode);

        if (patchNode.has("topics")) {
            topicService.createManyWithProblem(problemId, patchNode.get("topics"));
        }

        try {
            return problemRepository.save(problem);
        } catch (DuplicateKeyException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem already exists");
        }
    }

    @Transactional
    public void publish(long problemId) {
        if (testCaseRepository.countAllByProblemId(problemId) == 0) {
            throw new NoTestCasesException(problemId);
        }

        problemRepository.save(findOrThrow(problemId).withPublishedAt(Instant.now()));
    }

    @Transactional
    public void unpublish(long problemId) {
        problemRepository.save(findOrThrow(problemId).withPublishedAt(null));
    }

    private Problem findOrThrow(long problemid) {
        return problemRepository.findById(problemid)
                .orElseThrow(() -> new ProblemNotFoundException(problemid));
    }
}
