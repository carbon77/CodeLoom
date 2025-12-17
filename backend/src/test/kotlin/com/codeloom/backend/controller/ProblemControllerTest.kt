package com.codeloom.backend.controller

import com.codeloom.backend.dto.CreateProblemRequest
import com.codeloom.backend.dto.ProblemListItem
import com.codeloom.backend.model.Problem
import com.codeloom.backend.model.ProblemDifficulty
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Nested
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.*
import kotlin.test.Test

class ProblemControllerTest : BaseControllerTest() {
    companion object {
        private val id1 = UUID.randomUUID()
        private val id2 = UUID.randomUUID()

        private val problem1 = Problem(
            id = id1,
            name = "Two Sum",
            difficulty = ProblemDifficulty.EASY,
        )
        private val problem2 = Problem(
            id = id2,
            name = "Longest common subsequence",
            text = "Given two string, find lcs",
            difficulty = ProblemDifficulty.MEDIUM,
        )
        private val problems = listOf(problem1, problem2)
        private val problemItems = problems.map { ProblemListItem(it.id!!, it.name, it.difficulty) }
    }

    @Nested
    inner class FindAllItems {
        @Test
        fun `test without filters should return all problems `() {
            `when`(problemService.findItemsByFilters()).thenReturn(problemItems)
            mockMvc.get("/v1/problems/items")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()", equalTo(2))
                    jsonPath("$[0].id", equalTo(id1.toString()))
                    jsonPath("$[0].difficulty", equalTo(ProblemDifficulty.EASY.toString()))
                    jsonPath("$[0].name", equalTo("Two Sum"))

                    jsonPath("$[1].id", equalTo(id2.toString()))
                    jsonPath("$[1].difficulty", equalTo(ProblemDifficulty.MEDIUM.toString()))
                    jsonPath("$[1].name", equalTo("Longest common subsequence"))
                }

            verify(problemService).findItemsByFilters()
        }

        @Test
        fun `test with one filter should return 1 problem`() {
            val difficulties = listOf(ProblemDifficulty.EASY)
            `when`(problemService.findItemsByFilters(difficulties))
                .thenReturn(listOf(problemItems[0]))
            mockMvc
                .get("/v1/problems/items") {
                    param("difficulties", "EASY")
                }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()", equalTo(1))
                    jsonPath("$[0].id", equalTo(id1.toString()))
                    jsonPath("$[0].difficulty", equalTo(ProblemDifficulty.EASY.toString()))
                    jsonPath("$[0].name", equalTo("Two Sum"))
                }

            verify(problemService).findItemsByFilters(difficulties)
        }

        @Test
        fun `test with no problems should return empty array`() {
            `when`(problemService.findItemsByFilters())
                .thenReturn(listOf())
            mockMvc
                .get("/v1/problems/items")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()", equalTo(0))
                }

            verify(problemService).findItemsByFilters()
        }
    }

    @Nested
    inner class FindById {

        @Test
        fun `test should return problem`() {
            `when`(problemService.findById(id1)).thenReturn(problem1)
            mockMvc.get("/v1/problems/$id1")
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.id", equalTo(id1.toString()))
                    jsonPath("$.name", equalTo("Two Sum"))
                    jsonPath("$.text", equalTo(""))
                    jsonPath("$.difficulty", equalTo("EASY"))
                }
            verify(problemService).findById(id1)
        }

        @Test
        fun `test with non-existing uuid should return 404`() {
            mockMvc.get("/v1/problems/${UUID.randomUUID()}")
                .andExpect {
                    status { isNotFound() }
                }
        }

        @Test
        fun `test with invalid uuid should return 400`() {
            mockMvc.get("/v1/problems/123")
                .andExpect {
                    status { isBadRequest() }
                }
        }
    }

    @Nested
    inner class Create {

        @Test
        fun `test should return problem`() {
            val req = CreateProblemRequest("Two Sum")
            `when`(problemService.create(req)).thenReturn(problem1)
            mockMvc.post("/v1/problems") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(req)
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.id", equalTo(id1.toString()))
                    jsonPath("$.name", equalTo("Two Sum"))
                    jsonPath("$.text", equalTo(""))
                    jsonPath("$.difficulty", equalTo("EASY"))
                }
        }
    }
}