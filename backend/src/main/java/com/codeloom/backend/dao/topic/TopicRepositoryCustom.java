package com.codeloom.backend.dao.topic;

import com.codeloom.backend.model.*;
import java.util.Collection;

public interface TopicRepositoryCustom {
  Iterable<Topic> findByProblemId(long id);

  void saveAllProblemRelationships(Collection<ProblemTopicRelationship> r);

  void deleteRelationshipsWithProblem(long id);
}
