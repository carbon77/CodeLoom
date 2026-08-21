package com.codeloom.backend.transformer;

import static com.codeloom.backend.PatchValues.value;

import com.codeloom.backend.dao.testcase.TestCaseRepository;
import com.codeloom.backend.dao.topic.TopicRepository;
import com.codeloom.backend.dto.ProblemDto;
import com.codeloom.backend.model.*;
import java.util.stream.StreamSupport;
import org.springframework.stereotype.Component;
import tools.jackson.databind.*;

@Component
public class ProblemTransformer {
  private final TopicRepository topics;
  private final TestCaseRepository tests;
  private final ObjectMapper mapper;

  public ProblemTransformer(TopicRepository t, TestCaseRepository c, ObjectMapper m) {
    topics = t;
    tests = c;
    mapper = m;
  }

  public ProblemDto getDtoFromEntity(Problem p) {
    long id = p.getId();
    return new ProblemDto(
        id,
        p.getSlug(),
        p.getTitle(),
        p.getDescription(),
        p.getDifficulty(),
        p.getConstraints(),
        p.getExamples(),
        p.getHints(),
        tests.findAllByProblemId(id, true),
        topics.findByProblemId(id));
  }

  public Problem updateEntityFromPatchNode(Problem p, JsonNode n) {
    String[] hints = p.getHints();
    JsonNode h = n.get("hints");
    if (h != null && h.isArray())
      hints =
          StreamSupport.stream(h.spliterator(), false)
              .map(JsonNode::asString)
              .toArray(String[]::new);
    return p.updated(
        value(n, "slug", mapper, p.getSlug(), String.class),
        value(n, "title", mapper, p.getTitle(), String.class),
        value(n, "description", mapper, p.getDescription(), String.class),
        value(n, "difficulty", mapper, p.getDifficulty(), ProblemDifficulty.class),
        value(n, "constraints", mapper, p.getConstraints(), ProblemConstraints.class),
        value(n, "examples", mapper, p.getExamples(), ProblemExamples.class),
        hints);
  }
}
