package com.codeloom.backend.service;

import com.codeloom.backend.dao.topic.TopicRepository;
import com.codeloom.backend.dao.topic.TopicRepositoryCustomImpl;
import com.codeloom.backend.dto.CreateTopicRequest;
import com.codeloom.backend.dto.UpdateProblemTopicRequest;
import com.codeloom.backend.dto.UpdateTopicRequest;
import com.codeloom.backend.model.ProblemTopicRelationship;
import com.codeloom.backend.model.Topic;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class TopicService {
    private final TopicRepositoryCustomImpl topicRepositoryCustom;
    private final TopicRepository topicRepository;

    public Iterable<Topic> findAll() {
        return topicRepository.findAll();
    }

    public Topic findById(UUID topicId) {
        return topicRepository
                .findById(topicId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Entity with id `" + topicId + "` not found"));
    }

    @Transactional
    public Topic create(CreateTopicRequest request) {
        return topicRepository.save(Topic.builder().name(request.name()).build());
    }

    @Transactional
    public void createManyWithProblem(long problemId, List<UpdateProblemTopicRequest> topics) {
        topicRepositoryCustom.deleteRelationshipsWithProblem(problemId);
        List<ProblemTopicRelationship> relationships = new ArrayList<>();

        for (UpdateProblemTopicRequest topic : topics) {
            UUID topicId = null;
            if (topic.topic_id() != null) {
                try {
                    topicId = UUID.fromString(topic.topic_id());
                } catch (IllegalArgumentException ignored) {
                    // Ignore malformed references, matching missing topic references.
                }
            } else if (topic.name() != null) {
                topicId = topicRepository
                        .save(Topic.builder().name(topic.name()).build())
                        .getId();
            }

            if (topicId != null) {
                relationships.add(ProblemTopicRelationship.builder()
                        .topicId(topicId)
                        .problemId(problemId)
                        .build());
            }
        }
        topicRepositoryCustom.saveAllProblemRelationships(relationships);
    }

    @Transactional
    public Topic update(UUID topicId, UpdateTopicRequest request) {
        Topic topic = findById(topicId).withName(request.name());
        return topicRepository.save(topic);
    }

    @Transactional
    public void delete(UUID topicId) {
        topicRepository.deleteById(topicId);
    }
}
