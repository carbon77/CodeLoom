package com.codeloom.backend.service;

import com.codeloom.backend.dao.topic.TopicRepository;
import com.codeloom.backend.dao.topic.TopicRepositoryCustomImpl;
import com.codeloom.backend.dto.CreateTopicRequest;
import com.codeloom.backend.model.ProblemTopicRelationship;
import com.codeloom.backend.model.Topic;
import com.codeloom.backend.transformer.JsonTransformer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class TopicService {
    private final TopicRepositoryCustomImpl topicRepositoryCustom;
    private final TopicRepository topicRepository;
    private final ObjectMapper objectMapper;
    private final JsonTransformer jsonTransformer;

    public Iterable<Topic> findAll() {
        return topicRepository.findAll();
    }

    public Topic findById(UUID topicId) {
        return topicRepository.findById(topicId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `" + topicId + "` not found"));
    }

    @Transactional
    public Topic create(CreateTopicRequest request) {
        return topicRepository.save(
                Topic.builder()
                        .name(request.name())
                        .build()
        );
    }

    @Transactional
    public void createManyWithProblem(long problemId, JsonNode topicNodes) {
        topicRepositoryCustom.deleteRelationshipsWithProblem(problemId);
        List<ProblemTopicRelationship> relationships = new ArrayList<>();

        for (JsonNode node : topicNodes) {
            UUID topicId = null;
            if (node.has("topic_id")) {
                topicId = UUID.fromString(node.get("topic_id").asString());
            } else if (node.has("name")) {
                topicId = topicRepository.save(
                        Topic.builder()
                                .name(node.get("name").asString())
                                .build()
                ).getId();
            }

            if (topicId != null) {
                relationships.add(
                        ProblemTopicRelationship.builder()
                                .topicId(topicId)
                                .problemId(problemId)
                                .build()
                );
            }
        }
        topicRepositoryCustom.saveAllProblemRelationships(relationships);
    }

    @Transactional
    public Topic patch(UUID topicId, JsonNode patchNode) {
        Topic topic = findById(topicId)
                .withName(jsonTransformer.fromNodeToType(patchNode, "name", String.class));
        return topicRepository.save(topic);
    }

    @Transactional
    public void delete(UUID topicId) {
        topicRepository.deleteById(topicId);
    }
}
