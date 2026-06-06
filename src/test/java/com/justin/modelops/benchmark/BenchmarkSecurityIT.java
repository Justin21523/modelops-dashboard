package com.justin.modelops.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.justin.modelops.benchmark.dto.CreateBenchmarkRequest;
import com.justin.modelops.benchmark.enums.BenchmarkType;
import com.justin.modelops.support.AbstractPostgresContainerTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies role-based authorization on benchmark mutations end-to-end through the web
 * layer and security filter chain.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BenchmarkSecurityIT extends AbstractPostgresContainerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    private String body(String name) throws Exception {
        return objectMapper.writeValueAsString(
                new CreateBenchmarkRequest(name, BenchmarkType.CODING, "desc", null));
    }

    @Test
    @WithMockUser(username = "regular", roles = "USER")
    void nonAdminCannotCreateBenchmark() throws Exception {
        mockMvc.perform(post("/api/v1/benchmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("user-attempt")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "boss", roles = "ADMIN")
    void adminCanCreateBenchmark() throws Exception {
        mockMvc.perform(post("/api/v1/benchmarks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body("admin-coding-benchmark")))
                .andExpect(status().isCreated());
    }
}
