package com.codeloom.backend.controller

import com.codeloom.backend.model.Topic
import com.codeloom.backend.service.TopicService
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import tools.jackson.databind.JsonNode
import java.io.IOException
import java.util.UUID

@RestController
@RequestMapping("/v1/topics")
class TopicController(
    private val topicService: TopicService,
) {
    @GetMapping
    fun getAll(): Iterable<Topic> = topicService.getAll()

    @GetMapping("/{id}")
    fun getOne(
        @PathVariable id: UUID,
    ): Topic = topicService.getOne(id)

    @PostMapping
    fun create(
        @RequestBody topic: Topic,
    ): Topic = topicService.create(topic)

    @PatchMapping("/{id}")
    @Throws(IOException::class)
    fun patch(
        @PathVariable id: UUID,
        @RequestBody patchNode: JsonNode,
    ): Topic = topicService.patch(id, patchNode)

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable id: UUID,
    ) = topicService.delete(id)
}
