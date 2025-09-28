package com.gm2dev.demo_spring.controller.user;

import com.gm2dev.demo_spring.dto.ApiGenericResponse;
import com.gm2dev.demo_spring.dto.user.JwtAuthenticationResponse;
import com.gm2dev.demo_spring.dto.user.LoginRequest;
import com.gm2dev.demo_spring.dto.user.SignUpRequest;
import com.gm2dev.demo_spring.entity.user.User;
import com.gm2dev.demo_spring.mapper.user.UserMapper;
import com.gm2dev.demo_spring.service.user.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthService authService;
    private final UserMapper userMapper;

    @PostMapping("/signin")
    @Operation(summary = "Sign in user", description = "Authenticate user and return JWT token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully authenticated"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<JwtAuthenticationResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        String jwt = authService.authenticateUser(loginRequest.getUsernameOrEmail(), loginRequest.getPassword());
        Long expiration = authService.getJwtExpiration();

        return ResponseEntity.ok(new JwtAuthenticationResponse(jwt, expiration));
    }

    @PostMapping("/signup")
    @Operation(summary = "Sign up user", description = "Register a new user account")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Username or email already exists")
    })
    public ResponseEntity<ApiGenericResponse> registerUser(@Valid @RequestBody SignUpRequest signUpRequest) {
        User registeredUser = authService.registerUser(
            signUpRequest.getUsername(),
            signUpRequest.getEmail(),
            signUpRequest.getPassword(),
            signUpRequest.getFirstName(),
            signUpRequest.getLastName()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiGenericResponse(true, "User registered successfully"));
    }
}