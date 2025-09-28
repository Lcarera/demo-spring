package com.gm2dev.demo_spring.integration.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gm2dev.demo_spring.dto.user.LoginRequest;
import com.gm2dev.demo_spring.dto.user.SignUpRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@Testcontainers
@ActiveProfiles("test")
@Transactional
class AuthIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void registerAndSignIn_ValidFlow_Success() throws Exception {
        // Given
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("integrationuser");
        signUpRequest.setEmail("integration@example.com");
        signUpRequest.setPassword("password123");
        signUpRequest.setFirstName("Integration");
        signUpRequest.setLastName("Test");

        // When - Register user
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        // Then - Sign in with registered user
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("integrationuser");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").exists());
    }

    @Test
    void register_DuplicateUsername_ReturnsError() throws Exception {
        // Given - Register first user
        SignUpRequest firstUser = new SignUpRequest();
        firstUser.setUsername("duplicateuser");
        firstUser.setEmail("first@example.com");
        firstUser.setPassword("password123");

        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstUser)))
                .andExpect(status().isCreated());

        // When - Try to register with same username
        SignUpRequest duplicateUser = new SignUpRequest();
        duplicateUser.setUsername("duplicateuser");
        duplicateUser.setEmail("second@example.com");
        duplicateUser.setPassword("password123");

        // Then
        mockMvc.perform(post("/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username is already taken!"));
    }
}