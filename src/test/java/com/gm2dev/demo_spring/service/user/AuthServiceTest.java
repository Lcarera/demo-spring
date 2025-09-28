package com.gm2dev.demo_spring.service.user;

import com.gm2dev.demo_spring.config.JwtProperties;
import com.gm2dev.demo_spring.entity.user.Role;
import com.gm2dev.demo_spring.entity.user.User;
import com.gm2dev.demo_spring.exception.BadRequestException;
import com.gm2dev.demo_spring.repository.user.RoleRepository;
import com.gm2dev.demo_spring.repository.user.UserRepository;
import com.gm2dev.demo_spring.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private Role userRole;

    @BeforeEach
    void setUp() {
        userRole = new Role(Role.RoleName.USER, "Default user role");
        userRole.setId(1L);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
    }

    @Test
    void authenticateUser_ValidCredentials_ReturnsJwt() {
        // Given
        String usernameOrEmail = "testuser";
        String password = "password123";
        String expectedJwt = "jwt.token.here";

        Authentication mockAuthentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(tokenProvider.generateToken(mockAuthentication)).thenReturn(expectedJwt);

        // When
        String actualJwt = authService.authenticateUser(usernameOrEmail, password);

        // Then
        assertThat(actualJwt).isEqualTo(expectedJwt);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateToken(mockAuthentication);
    }

    @Test
    void registerUser_ValidData_ReturnsUser() {
        // Given
        String username = "newuser";
        String email = "new@example.com";
        String password = "password123";
        String firstName = "John";
        String lastName = "Doe";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(roleRepository.findByName(Role.RoleName.USER)).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            return user;
        });

        // When
        User result = authService.registerUser(username, email, password, firstName, lastName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getFirstName()).isEqualTo(firstName);
        assertThat(result.getLastName()).isEqualTo(lastName);
        assertThat(result.getPassword()).isEqualTo("encodedPassword");

        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder).encode(password);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_UsernameExists_ThrowsBadRequestException() {
        // Given
        String username = "existinguser";
        String email = "new@example.com";
        String password = "password123";

        when(userRepository.existsByUsername(username)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.registerUser(username, email, password, "John", "Doe"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Username is already taken!");

        verify(userRepository).existsByUsername(username);
        verify(userRepository, never()).save(any());
    }

    @Test
    void registerUser_EmailExists_ThrowsBadRequestException() {
        // Given
        String username = "newuser";
        String email = "existing@example.com";
        String password = "password123";

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authService.registerUser(username, email, password, "John", "Doe"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Email Address already in use!");

        verify(userRepository).existsByUsername(username);
        verify(userRepository).existsByEmail(email);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getJwtExpiration_ReturnsExpiration() {
        // Given
        Long expectedExpiration = 86400000L;
        when(jwtProperties.getExpiration()).thenReturn(expectedExpiration);

        // When
        Long actualExpiration = authService.getJwtExpiration();

        // Then
        assertThat(actualExpiration).isEqualTo(expectedExpiration);
    }
}