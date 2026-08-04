package com.codeloom.backend.controller

import com.codeloom.backend.dto.CreateTopicRequest
import com.codeloom.backend.model.Topic
import com.codeloom.backend.service.TopicService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import tools.jackson.databind.JsonNode
import java.util.*

@RestController
@RequestMapping("/v1/topics")
class TopicController(private val topicService: TopicService) {
    @GetMapping
    fun findAll(): Iterable<Topic> = topicService.findAll()

    @GetMapping("/{id}")
    fun findAll(@PathVariable id: UUID): Topic = topicService.findById(id)

    @PostMapping
    fun create(@Valid @RequestBody request: CreateTopicRequest): Topic = topicService.create(request)

    @PatchMapping("/{id}")
    fun patch(@PathVariable id: UUID, @RequestBody patchNode: JsonNode): Topic = topicService.patch(id, patchNode)

    @DeleteMapping("/{id}")
    fun delete(@PathVariable id: UUID) = topicService.delete(id)
}
