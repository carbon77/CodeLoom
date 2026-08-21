package com.codeloom.backend.controller;

import com.codeloom.backend.dto.CreateTopicRequest;
import com.codeloom.backend.model.Topic;
import com.codeloom.backend.service.TopicService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/v1/topics")
public class TopicController {
  private final TopicService service;

  public TopicController(TopicService s) {
    service = s;
  }

  @GetMapping
  public Iterable<Topic> findAll() {
    return service.findAll();
  }

  @GetMapping("/{id}")
  public Topic findAll(@PathVariable UUID id) {
    return service.findById(id);
  }

  @PostMapping
  public Topic create(@Valid @RequestBody CreateTopicRequest q) {
    return service.create(q);
  }

  @PatchMapping("/{id}")
  public Topic patch(@PathVariable UUID id, @RequestBody JsonNode n) {
    return service.patch(id, n);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable UUID id) {
    service.delete(id);
  }
}
