package com.codeloom.backend.it;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.codeloom.backend.dao.topic.TopicRepository;
import com.codeloom.backend.model.Topic;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;

@Sql(statements = "TRUNCATE TABLE topics CASCADE", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TopicIT extends BackendIntegrationTestSupport {
    @Autowired
    TopicRepository topics;

    private Topic topic(String name) {
        return topics.save(Topic.builder().name(name).build());
    }

    @Nested
    class FindAll {
        @Test
        void empty() throws Exception {
            mockMvc.perform(get("/v1/topics"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(0));
        }

        @Test
        void returnsTopics() throws Exception {
            topic("Arrays");
            topic("Dynamic Programming");
            mockMvc.perform(get("/v1/topics"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].name").value("Arrays"))
                    .andExpect(jsonPath("$[1].name").value("Dynamic Programming"));
        }
    }

    @Nested
    class FindOne {
        @Test
        void returnsTopic() throws Exception {
            var topic = topic("Graphs");
            mockMvc.perform(get("/v1/topics/{id}", topic.getId()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(topic.getId().toString()))
                    .andExpect(jsonPath("$.name").value(topic.getName()));
        }

        @Test
        void missingTopicIsNotFound() throws Exception {
            mockMvc.perform(get("/v1/topics/{id}", UUID.randomUUID())).andExpect(status().isNotFound());
        }
    }

    @Nested
    class Create {
        @Test
        void createsTopic() throws Exception {
            mockMvc.perform(post("/v1/topics")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"Greedy\"}"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", notNullValue()))
                    .andExpect(jsonPath("$.name").value("Greedy"));
            assertEquals(1, topics.count());
        }

        @Test
        void blankNameIsBadRequest() throws Exception {
            mockMvc.perform(post("/v1/topics")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.status").value(400));
            assertEquals(0, topics.count());
        }
    }

    @Nested
    class Update {
        @Test
        void updatesTopic() throws Exception {
            var topic = topic("Old Name");
            mockMvc.perform(put("/v1/topics/{id}", topic.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\":\"New Name\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(topic.getId().toString()))
                    .andExpect(jsonPath("$.name").value("New Name"));
            assertEquals(
                    "New Name", topics.findById(topic.getId()).orElseThrow().getName());
        }

        @Test
        void rejectsIncompleteUpdate() throws Exception {
            var topic = topic("Old Name");
            mockMvc.perform(put("/v1/topics/{id}", topic.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }
    }

    @Nested
    class Delete {
        @Test
        void deletesTopic() throws Exception {
            var topic = topic("Bit Manipulation");
            mockMvc.perform(delete("/v1/topics/{id}", topic.getId())).andExpect(status().isOk());
            assertEquals(0, topics.count());
        }
    }
}
