package com.codeloom.backend.transformer;

import com.codeloom.backend.dao.testcase.TestCaseRepository;
import com.codeloom.backend.dao.topic.TopicRepository;
import com.codeloom.backend.dto.ProblemDto;
import com.codeloom.backend.model.Problem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemTransformer {
    private final TopicRepository topicRepository;
    private final TestCaseRepository testCaseRepository;

    public ProblemDto getDtoFromEntity(Problem problem) {
        long problemId = problem.getId();
        return ProblemDto.builder()
                .id(problemId)
                .slug(problem.getSlug())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .difficulty(problem.getDifficulty())
                .constraints(problem.getConstraints())
                .examples(problem.getExamples())
                .hints(problem.getHints())
                .testCases(testCaseRepository.findAllByProblemId(problemId, true))
                .topics(topicRepository.findByProblemId(problemId))
                .build();
    }
}
