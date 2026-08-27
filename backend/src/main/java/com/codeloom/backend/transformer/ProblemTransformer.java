package com.codeloom.backend.transformer;

import com.codeloom.backend.dao.testcase.TestCaseRepository;
import com.codeloom.backend.dao.topic.TopicRepository;
import com.codeloom.backend.dto.ProblemDto;
import com.codeloom.backend.dto.TestCaseDto;
import com.codeloom.backend.model.Problem;
import com.codeloom.backend.model.Topic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProblemTransformer {
    private final TopicRepository topicRepository;
    private final TestCaseRepository testCaseRepository;

    public ProblemDto getDtoFromEntity(Problem problem) {
        long problemId = problem.getId();

        Iterable<Topic> topics = topicRepository.findByProblemId(problemId);
        List<TestCaseDto> examples = testCaseRepository.findAllByProblemId(problemId, true)
                .stream()
                .map(TestCaseDto::fromEntity)
                .toList();

        return ProblemDto.builder()
                .id(problemId)
                .slug(problem.getSlug())
                .title(problem.getTitle())
                .description(problem.getDescription())
                .difficulty(problem.getDifficulty())
                .constraints(problem.getConstraints())
                .examples(examples)
                .hints(problem.getHints())
                .topics(topics)
                .build();
    }
}
