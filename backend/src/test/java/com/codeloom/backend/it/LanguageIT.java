package com.codeloom.backend.it;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

class LanguageIT extends BackendIntegrationTestSupport {
    @Test
    void returnsDisplayInformation() throws Exception {
        mockMvc.perform(get("/v1/languages/display"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("cpp"))
                .andExpect(jsonPath("$[0].name").value("C++"))
                .andExpect(jsonPath("$[1].key").value("java"))
                .andExpect(jsonPath("$[2].key").value("python"))
                .andExpect(jsonPath("$[0].image").doesNotExist());
    }

    @Test
    void returnsCommandsWithoutDockerInformation() throws Exception {
        mockMvc.perform(get("/v1/languages/system"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].key").value("cpp"))
                .andExpect(jsonPath("$[0].compileCommand").value("g++ -std=c++20 -O2 -o main main.cpp"))
                .andExpect(jsonPath("$[0].runCommand").value("./main"))
                .andExpect(jsonPath("$[2].compileCommand").isEmpty())
                .andExpect(jsonPath("$[2].runCommand").value("python3 main.py"))
                .andExpect(jsonPath("$[0].runImage").doesNotExist())
                .andExpect(jsonPath("$[0].compileImage").doesNotExist())
                .andExpect(jsonPath("$[0].sourceFile").doesNotExist());
    }
}
