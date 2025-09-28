package com.gm2dev.demo_spring.service.user;

import com.gm2dev.demo_spring.config.JwtProperties;
import com.gm2dev.demo_spring.entity.user.Role;
import com.gm2dev.demo_spring.entity.user.User;
import com.gm2dev.demo_spring.exception.BadRequestException;
import com.gm2dev.demo_spring.repository.user.RoleRepository;
import com.gm2dev.demo_spring.repository.user.UserRepository;
import com.gm2dev.demo_spring.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final JwtProperties jwtProperties;

    @Transactional
    public String authenticateUser(String usernameOrEmail, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(usernameOrEmail, password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        log.info("User {} authenticated successfully", usernameOrEmail);

        return jwt;
    }

    @Transactional
    public User registerUser(String username, String email, String password, String firstName, String lastName) {
        if (userRepository.existsByUsername(username)) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email Address already in use!");
        }

        // Create user's account
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);

        Role userRole = roleRepository.findByName(Role.RoleName.USER)
                .orElseThrow(() -> new RuntimeException("User Role not found."));

        user.setRoles(Collections.singleton(userRole));

        User result = userRepository.save(user);

        log.info("User {} registered successfully with ID: {}", result.getUsername(), result.getId());

        return result;
    }

    public Long getJwtExpiration() {
        return jwtProperties.getExpiration();
    }
}