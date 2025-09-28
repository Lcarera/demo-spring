package com.gm2dev.demo_spring.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gm2dev.demo_spring.dto.user.LoginRequest;
import com.gm2dev.demo_spring.dto.user.SignUpRequest;
import com.gm2dev.demo_spring.entity.user.User;
import com.gm2dev.demo_spring.mapper.user.UserMapper;
import com.gm2dev.demo_spring.service.user.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private UserMapper userMapper;

    private LoginRequest loginRequest;
    private SignUpRequest signUpRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("testuser");
        loginRequest.setPassword("password123");

        signUpRequest = new SignUpRequest();
        signUpRequest.setUsername("newuser");
        signUpRequest.setEmail("new@example.com");
        signUpRequest.setPassword("password123");
        signUpRequest.setFirstName("John");
        signUpRequest.setLastName("Doe");

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("newuser");
        testUser.setEmail("new@example.com");
    }

    @Test
    @WithMockUser
    void authenticateUser_ValidCredentials_ReturnsJwtResponse() throws Exception {
        // Given
        String expectedJwt = "jwt.token.here";
        Long expiration = 86400000L;

        when(authService.authenticateUser(anyString(), anyString())).thenReturn(expectedJwt);
        when(authService.getJwtExpiration()).thenReturn(expiration);

        // When & Then
        mockMvc.perform(post("/auth/signin")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(expectedJwt))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(expiration));
    }

    @Test
    @WithMockUser
    void authenticateUser_InvalidCredentials_ReturnsBadRequest() throws Exception {
        // Given
        LoginRequest invalidRequest = new LoginRequest();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/auth/signin")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void registerUser_ValidData_ReturnsSuccessResponse() throws Exception {
        // Given
        when(authService.registerUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(testUser);

        // When & Then
        mockMvc.perform(post("/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signUpRequest)))
                .andExpected(status().isCreated())
                .andExpected(jsonPath("$.success").value(true))
                .andExpected(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    @WithMockUser
    void registerUser_InvalidData_ReturnsBadRequest() throws Exception {
        // Given
        SignUpRequest invalidRequest = new SignUpRequest();
        invalidRequest.setUsername("u"); // Too short
        invalidRequest.setEmail("invalid-email"); // Invalid format
        invalidRequest.setPassword("123"); // Too short

        // When & Then
        mockMvc.perform(post("/auth/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}