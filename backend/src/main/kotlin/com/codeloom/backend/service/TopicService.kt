package com.codeloom.backend.service

import com.codeloom.backend.dao.topic.TopicRepository
import com.codeloom.backend.dao.topic.TopicRepositoryCustomImpl
import com.codeloom.backend.dto.CreateTopicRequest
import com.codeloom.backend.model.ProblemTopicRelationship
import com.codeloom.backend.model.Topic
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import java.util.*

@Service
class TopicService(
    private val topicRepositoryCustomImpl: TopicRepositoryCustomImpl,
    private val topicRepository: TopicRepository,
    private val objectMapper: ObjectMapper,
) {
    fun findAll(): Iterable<Topic> = topicRepository.findAll()

    fun findById(id: UUID): Topic = findOrThrow(id)

    @Transactional
    fun create(request: CreateTopicRequest): Topic {
        val topic = Topic(name = request.name)
        return topicRepository.save(topic)
    }

    @Transactional
    fun createManyWithProblem(
        problemId: Long,
        node: JsonNode,
    ) {
        topicRepositoryCustomImpl.deleteRelationshipsWithProblem(problemId)
        val problemTopicRelationships =
            node.asIterable()
                .mapNotNull {
                    when {
                        it.has("topic_id") -> {
                            try {
                                UUID.fromString(it["topic_id"].asString())
                            } catch (_: IllegalArgumentException) {
                                null
                            }
                        }

                        it.has("name") -> {
                            val topic = Topic(name = it["name"].asString())
                            topicRepository.save(topic).id!!
                        }

                        else -> null
                    }
                }
                .map { ProblemTopicRelationship(it, problemId) }
        topicRepositoryCustomImpl.saveAllProblemRelationships(problemTopicRelationships)
    }

    @Transactional
    fun patch(
        id: UUID,
        patchNode: JsonNode,
    ): Topic {
        val topic = findOrThrow(id)
        objectMapper.readerForUpdating(topic).readValue<Topic>(patchNode)
        return topicRepository.save(topic)
    }

    @Transactional
    fun delete(id: UUID) {
        topicRepository.deleteById(id)
    }

    private fun findOrThrow(id: UUID): Topic {
        return topicRepository.findById(id)
            .orElseThrow {
                ResponseStatusException(HttpStatus.NOT_FOUND, "Entity with id `$id` not found")
            }
    }
}
