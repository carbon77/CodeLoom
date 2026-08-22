package com.codeloom.backend.controller;

import com.codeloom.backend.dto.CreateTopicRequest;
import com.codeloom.backend.dto.UpdateTopicRequest;
import com.codeloom.backend.model.Topic;
import com.codeloom.backend.service.TopicService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @PutMapping("/{id}")
    public Topic update(@PathVariable UUID id, @Valid @RequestBody UpdateTopicRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        service.delete(id);
    }
}
