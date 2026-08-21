package com.codeloom.backend.controller;

import com.codeloom.backend.dto.CreateTopicRequest;
import com.codeloom.backend.model.Topic;
import com.codeloom.backend.service.TopicService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

@RestController
@RequestMapping("/v1/topics")
@RequiredArgsConstructor
public class TopicController {
    private final TopicService service;

    @GetMapping
    public Iterable<Topic> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public Topic findAll(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PostMapping
    public Topic create(@Valid @RequestBody CreateTopicRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{id}")
    public Topic patch(@PathVariable UUID id, @RequestBody JsonNode patchJsonNode) {
        return service.patch(id, patchJsonNode);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
