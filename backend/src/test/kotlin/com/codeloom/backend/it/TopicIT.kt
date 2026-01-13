package com.codeloom.backend.it

import com.codeloom.backend.BaseTest
import com.codeloom.backend.dao.TopicRepository
import com.codeloom.backend.model.Topic
import org.hamcrest.Matchers
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.servlet.*
import java.util.*
import kotlin.test.assertEquals

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@Sql(
    statements = [
        "TRUNCATE TABLE topics CASCADE"
    ],
    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
)
class TopicIT : BaseTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var topicRepository: TopicRepository

    private fun initTopic(name: String = "Arrays"): Topic =
        topicRepository.save(
            Topic(
                name = name
            )
        )

    /* -------------------------------------------------------------
     * GET /v1/topics
     * ------------------------------------------------------------- */
    @Nested
    inner class FindAll {

        @Test
        fun `test with no topics should return empty array`() {
            mockMvc.get("/v1/topics")
                .andExpect {
                    status { isOk() }
                    content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()", Matchers.equalTo(0))
                }
        }

        @Test
        fun `test with topics should return array`() {
            initTopic("Arrays")
            initTopic("Dynamic Programming")

            mockMvc.get("/v1/topics")
                .andExpect {
                    status { isOk() }
                    content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                    jsonPath("$.length()", Matchers.equalTo(2))
                    jsonPath("$[0].name", Matchers.equalTo("Arrays"))
                    jsonPath("$[1].name", Matchers.equalTo("Dynamic Programming"))
                }
        }
    }

    /* -------------------------------------------------------------
     * GET /v1/topics/{id}
     * ------------------------------------------------------------- */
    @Nested
    inner class FindOne {

        @Test
        fun `test should return topic`() {
            val topic = initTopic("Graphs")

            mockMvc.get("/v1/topics/${topic.id}")
                .andExpect {
                    status { isOk() }
                    content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                    jsonPath("$.id", Matchers.equalTo(topic.id.toString()))
                    checkTopic(topic)
                }
        }

        @Test
        fun `test with non-existing id should return 404`() {
            mockMvc.get("/v1/topics/${UUID.randomUUID()}")
                .andExpect {
                    status { isNotFound() }
                }
        }
    }

    /* -------------------------------------------------------------
     * POST /v1/topics
     * ------------------------------------------------------------- */
    @Nested
    inner class Create {

        @Test
        fun `test should create topic`() {
            mockMvc.post("/v1/topics") {
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                      "name": "Greedy"
                    }
                """.trimIndent()
            }
                .andExpect {
                    status { isOk() }
                    content { contentTypeCompatibleWith(MediaType.APPLICATION_JSON) }
                    jsonPath("$.id", Matchers.notNullValue())
                    jsonPath("$.name", Matchers.equalTo("Greedy"))
                }

            assertEquals(1, topicRepository.count())
        }

    }

    /* -------------------------------------------------------------
     * PATCH /v1/topics/{id}
     * ------------------------------------------------------------- */
    @Nested
    inner class Patch {

        @Test
        fun `test should patch topic`() {
            val topic = initTopic("Old Name")

            mockMvc.patch("/v1/topics/${topic.id}") {
                contentType = MediaType.APPLICATION_JSON
                content = """
                    {
                      "name": "New Name"
                    }
                """.trimIndent()
            }
                .andExpect {
                    status { isOk() }
                    jsonPath("$.name", Matchers.equalTo("New Name"))
                }

            val updated = topicRepository.findById(topic.id!!).get()
            assertEquals("New Name", updated.name)
        }
    }

    /* -------------------------------------------------------------
     * DELETE /v1/topics/{id}
     * ------------------------------------------------------------- */
    @Nested
    inner class Delete {

        @Test
        fun `test should delete topic`() {
            val topic = initTopic("Bit Manipulation")

            mockMvc.delete("/v1/topics/${topic.id}")
                .andExpect {
                    status { isOk() }
                }

            assertEquals(0, topicRepository.count())
        }
    }

    /* -------------------------------------------------------------
     * JSON assertions
     * ------------------------------------------------------------- */
    private fun MockMvcResultMatchersDsl.checkTopic(topic: Topic) {
        jsonPath("$.id", Matchers.equalTo(topic.id.toString()))
        jsonPath("$.name", Matchers.equalTo(topic.name))
    }
}
