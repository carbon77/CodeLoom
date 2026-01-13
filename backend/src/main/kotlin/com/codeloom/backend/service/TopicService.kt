package com.codeloom.backend.service;

import com.codeloom.backend.dao.ProblemTopicRepository
import com.codeloom.backend.dao.TopicRepository
import com.codeloom.backend.model.ProblemTopic
import com.codeloom.backend.model.Topic
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.contains
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.io.IOException
import java.util.*

@Service
class TopicService(
    private val problemTopicRepository: ProblemTopicRepository,
    private val topicRepository: TopicRepository,
    private val objectMapper: ObjectMapper,
) {
    fun getAll(): Iterable<Topic> = topicRepository.findAll()
    fun getOne(id: UUID): Topic {
        val topicOptional: Optional<Topic> = topicRepository.findById(id)
        return topicOptional.orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `$id` not found")
        }
    }

    fun create(topic: Topic): Topic = topicRepository.save(topic)

    @Transactional
    fun createManyWithProblem(problemId: Long, node: JsonNode) {
        problemTopicRepository.deleteByProblemId(problemId)
        val problemTopics = node.asIterable()
            .mapNotNull {
                when {
                    it.contains("topic_id") -> {
                        try {
                            UUID.fromString(it["topic_id"].asText())
                        } catch (e: IllegalArgumentException) {
                            null
                        }
                    }

                    it.contains("name") -> {
                        topicRepository
                            .save(Topic(name = it["name"].asText()))
                            .id!!
                    }

                    else -> null
                }
            }
            .map { ProblemTopic(it, problemId) }
        problemTopicRepository.saveAll(problemTopics)
    }

    @Throws(IOException::class)
    fun patch(id: UUID, patchNode: JsonNode): Topic {
        val topic: Topic = topicRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `$id` not found")
        }
        objectMapper.readerForUpdating(topic).readValue<Topic>(patchNode)
        return topicRepository.save(topic)
    }

    fun delete(id: UUID) {
        topicRepository.deleteById(id)
    }
}