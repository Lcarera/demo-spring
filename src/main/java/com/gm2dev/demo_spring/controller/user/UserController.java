package com.gm2dev.demo_spring.controller.user;

import com.gm2dev.demo_spring.dto.user.UserResponse;
import com.gm2dev.demo_spring.entity.user.User;
import com.gm2dev.demo_spring.mapper.user.UserMapper;
import com.gm2dev.demo_spring.security.CurrentUser;
import com.gm2dev.demo_spring.security.UserPrincipal;
import com.gm2dev.demo_spring.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "User Management", description = "User management APIs")
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Get currently authenticated user information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved current user"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserResponse> getCurrentUser(@CurrentUser UserPrincipal currentUser) {
        User user = userService.getCurrentUser(currentUser);
        return ResponseEntity.ok(userMapper.toUserResponse(user));
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID", description = "Get user information by user ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserResponse> getUserById(
            @Parameter(description = "User ID") @PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return ResponseEntity.ok(userMapper.toUserResponse(user));
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "Get user by username", description = "Get user information by username")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserResponse> getUserByUsername(
            @Parameter(description = "Username") @PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(userMapper.toUserResponse(user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all users", description = "Get paginated list of all users (Admin only)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved users"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<User> users = userService.getAllUsers(pageable);
        Page<UserResponse> userResponses = users.map(userMapper::toUserResponse);
        return ResponseEntity.ok(userResponses);
    }

    @GetMapping("/check-username")
    @Operation(summary = "Check username availability", description = "Check if username is available for registration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Username availability checked")
    })
    public ResponseEntity<Boolean> checkUsernameAvailability(
            @Parameter(description = "Username to check") @RequestParam String username) {
        Boolean isAvailable = userService.isUsernameAvailable(username);
        return ResponseEntity.ok(isAvailable);
    }

    @GetMapping("/check-email")
    @Operation(summary = "Check email availability", description = "Check if email is available for registration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email availability checked")
    })
    public ResponseEntity<Boolean> checkEmailAvailability(
            @Parameter(description = "Email to check") @RequestParam String email) {
        Boolean isAvailable = userService.isEmailAvailable(email);
        return ResponseEntity.ok(isAvailable);
    }
}