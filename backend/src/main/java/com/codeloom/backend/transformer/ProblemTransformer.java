package com.codeloom.backend.transformer;

import com.codeloom.backend.dao.testcase.TestCaseRepository;
import com.codeloom.backend.dao.topic.TopicRepository;
import com.codeloom.backend.dto.ProblemDto;
import com.codeloom.backend.model.Problem;
import com.codeloom.backend.model.ProblemConstraints;
import com.codeloom.backend.model.ProblemDifficulty;
import com.codeloom.backend.model.ProblemExamples;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.util.stream.StreamSupport;


@Component
@RequiredArgsConstructor
public class ProblemTransformer {
    private final TopicRepository topicRepository;
    private final TestCaseRepository testCaseRepository;
    private final JsonTransformer jsonTransformer;

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

    public Problem updateEntityFromPatchNode(Problem problem, JsonNode patchNode) {
        String[] hints = problem.getHints();
        JsonNode hintsNode = patchNode.get("hints");

        if (hintsNode != null && hintsNode.isArray()) {
            hints = StreamSupport.stream(hintsNode.spliterator(), false)
                    .map(JsonNode::asString)
                    .toArray(String[]::new);
        }

        return problem
                .withSlug(jsonTransformer.fromNodeToType(patchNode, "slug", String.class))
                .withTitle(jsonTransformer.fromNodeToType(patchNode, "title", String.class))
                .withDescription(jsonTransformer.fromNodeToType(patchNode, "description", String.class))
                .withDifficulty(jsonTransformer.fromNodeToType(patchNode, "difficulty", ProblemDifficulty.class))
                .withConstraints(jsonTransformer.fromNodeToType(patchNode, "constraints", ProblemConstraints.class))
                .withExamples(jsonTransformer.fromNodeToType(patchNode, "examples", ProblemExamples.class))
                .withHints(hints);
    }
}
