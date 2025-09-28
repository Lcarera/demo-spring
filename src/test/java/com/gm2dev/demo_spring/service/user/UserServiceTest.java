package com.gm2dev.demo_spring.service.user;

import com.gm2dev.demo_spring.entity.user.User;
import com.gm2dev.demo_spring.exception.ResourceNotFoundException;
import com.gm2dev.demo_spring.repository.user.UserRepository;
import com.gm2dev.demo_spring.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserPrincipal testUserPrincipal;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testUserPrincipal = new UserPrincipal(1L, "testuser", "test@example.com", "password", Collections.emptyList());
    }

    @Test
    void getCurrentUser_ValidPrincipal_ReturnsUser() {
        // Given
        when(userRepository.findByIdWithRoles(1L)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getCurrentUser(testUserPrincipal);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByIdWithRoles(1L);
    }

    @Test
    void getUserById_ExistingId_ReturnsUser() {
        // Given
        Long userId = 1L;
        when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserById(userId);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByIdWithRoles(userId);
    }

    @Test
    void getUserById_NonExistingId_ThrowsResourceNotFoundException() {
        // Given
        Long userId = 999L;
        when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id : '999'");

        verify(userRepository).findByIdWithRoles(userId);
    }

    @Test
    void getUserByUsername_ExistingUsername_ReturnsUser() {
        // Given
        String username = "testuser";
        when(userRepository.findByUsernameWithRoles(username)).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserByUsername(username);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).findByUsernameWithRoles(username);
    }

    @Test
    void getUserByUsername_NonExistingUsername_ThrowsResourceNotFoundException() {
        // Given
        String username = "nonexistent";
        when(userRepository.findByUsernameWithRoles(username)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserByUsername(username))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with username : 'nonexistent'");

        verify(userRepository).findByUsernameWithRoles(username);
    }

    @Test
    void getAllUsers_ValidPageable_ReturnsPageOfUsers() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(Collections.singletonList(testUser));
        when(userRepository.findAllWithRoles(pageable)).thenReturn(userPage);

        // When
        Page<User> result = userService.getAllUsers(pageable);

        // Then
        assertThat(result).isEqualTo(userPage);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualTo(testUser);
        verify(userRepository).findAllWithRoles(pageable);
    }

    @Test
    void isUsernameAvailable_NonExistingUsername_ReturnsTrue() {
        // Given
        String username = "available";
        when(userRepository.existsByUsername(username)).thenReturn(false);

        // When
        Boolean result = userService.isUsernameAvailable(username);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).existsByUsername(username);
    }

    @Test
    void isUsernameAvailable_ExistingUsername_ReturnsFalse() {
        // Given
        String username = "taken";
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // When
        Boolean result = userService.isUsernameAvailable(username);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).existsByUsername(username);
    }

    @Test
    void isEmailAvailable_NonExistingEmail_ReturnsTrue() {
        // Given
        String email = "available@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // When
        Boolean result = userService.isEmailAvailable(email);

        // Then
        assertThat(result).isTrue();
        verify(userRepository).existsByEmail(email);
    }

    @Test
    void isEmailAvailable_ExistingEmail_ReturnsFalse() {
        // Given
        String email = "taken@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When
        Boolean result = userService.isEmailAvailable(email);

        // Then
        assertThat(result).isFalse();
        verify(userRepository).existsByEmail(email);
    }

    @Test
    void createUser_ValidUser_ReturnsCreatedUser() {
        // Given
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        User result = userService.createUser(testUser);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUser_ValidUser_ReturnsUpdatedUser() {
        // Given
        when(userRepository.save(testUser)).thenReturn(testUser);

        // When
        User result = userService.updateUser(testUser);

        // Then
        assertThat(result).isEqualTo(testUser);
        verify(userRepository).save(testUser);
    }

    @Test
    void deleteUser_ExistingUser_DeletesUser() {
        // Given
        Long userId = 1L;
        when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.of(testUser));

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository).findByIdWithRoles(userId);
        verify(userRepository).delete(testUser);
    }
}