package com.codeloom.backend.service;

import com.codeloom.backend.dao.topic.*;
import com.codeloom.backend.dto.CreateTopicRequest;
import com.codeloom.backend.model.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.*;

@Service
public class TopicService {
    private final TopicRepositoryCustomImpl custom;
    private final TopicRepository repo;
    private final ObjectMapper mapper;

    public TopicService(TopicRepositoryCustomImpl c, TopicRepository r, ObjectMapper m) {
        custom = c;
        repo = r;
        mapper = m;
    }

    public Iterable<Topic> findAll() {
        return repo.findAll();
    }

    public Topic findById(UUID id) {
        return repo.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `" + id + "` not found"));
    }

    @Transactional
    public Topic create(CreateTopicRequest q) {
        return repo.save(new Topic(q.name()));
    }

    @Transactional
    public void createManyWithProblem(long id, JsonNode node) {
        custom.deleteRelationshipsWithProblem(id);
        List<ProblemTopicRelationship> rs = new ArrayList<>();
        for (JsonNode n : node) {
            UUID tid = null;
            if (n.has("topic_id")) {
                try {
                    tid = UUID.fromString(n.get("topic_id").asString());
                } catch (IllegalArgumentException ignored) {
                }
            } else if (n.has("name"))
                tid = repo.save(new Topic(n.get("name").asString())).getId();
            if (tid != null) rs.add(new ProblemTopicRelationship(tid, id));
        }
        custom.saveAllProblemRelationships(rs);
    }

    @Transactional
    public Topic patch(UUID id, JsonNode n) {
        Topic t = findById(id);
        mapper.readerForUpdating(t).readValue(n);
        return repo.save(t);
    }

    @Transactional
    public void delete(UUID id) {
        repo.deleteById(id);
    }
}
