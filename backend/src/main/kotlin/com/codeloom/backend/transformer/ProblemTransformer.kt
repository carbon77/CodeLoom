package com.codeloom.backend.transformer

import com.codeloom.backend.dao.testcase.TestCaseRepository
import com.codeloom.backend.dao.topic.TopicRepository
import com.codeloom.backend.dto.ProblemDto
import com.codeloom.backend.model.Problem
import com.codeloom.backend.patchValue
import org.springframework.stereotype.Component
import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper

@Component
class ProblemTransformer(
    private val topicRepository: TopicRepository,
    private val testCaseRepository: TestCaseRepository,
    private val objectMapper: ObjectMapper,
) {
    fun getDtoFromEntity(problem: Problem): ProblemDto {
        val problemId = problem.id!!
        val testCases = testCaseRepository.findAllByProblemId(problemId, true)
        val topics = topicRepository.findByProblemId(problemId)

        return ProblemDto(
            id = problemId,
            slug = problem.slug,
            title = problem.title,
            description = problem.description,
            difficulty = problem.difficulty,
            constraints = problem.constraints,
            examples = problem.examples,
            hints = problem.hints,
            testCases = testCases,
            topics = topics,
        )
    }

    fun updateEntityFromPatchNode(
        problem: Problem,
        patchNode: JsonNode,
    ): Problem {
        return problem.copy(
            title = patchNode.patchValue("title", objectMapper, problem.title),
            slug = patchNode.patchValue("slug", objectMapper, problem.slug),
            description = patchNode.patchValue("description", objectMapper, problem.description),
            difficulty = patchNode.patchValue("difficulty", objectMapper, problem.difficulty),
            hints =
                patchNode.get("hints")?.takeIf { it.isArray }?.asArray()?.values()?.map { it.asString() }
                    ?.toTypedArray()
                    ?: problem.hints,
            examples = patchNode.patchValue("examples", objectMapper, problem.examples),
            constraints = patchNode.patchValue("constraints", objectMapper, problem.constraints),
        )
    }
}
