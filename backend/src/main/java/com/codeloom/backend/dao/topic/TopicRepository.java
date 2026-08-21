package com.codeloom.backend.dao.topic;

import com.codeloom.backend.model.Topic;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;

public interface TopicRepository extends CrudRepository<Topic, UUID>, TopicRepositoryCustom {}
