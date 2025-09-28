package com.gm2dev.demo_spring.config;

import com.gm2dev.demo_spring.entity.user.Role;
import com.gm2dev.demo_spring.entity.user.Role.RoleName;
import com.gm2dev.demo_spring.entity.user.User;
import com.gm2dev.demo_spring.repository.user.RoleRepository;
import com.gm2dev.demo_spring.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Profile({"dev", "test"})
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Initializing development data...");

        // Create roles if they don't exist
        createRolesIfNotExist();

        // Create test users if they don't exist
        createTestUsersIfNotExist();

        log.info("Development data initialization completed");
    }

    private void createRolesIfNotExist() {
        for (RoleName roleName : RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = Role.builder()
                    .name(roleName)
                    .description(getDescriptionForRole(roleName))
                    .build();
                roleRepository.save(role);
                log.info("Created role: {}", roleName);
            }
        }
    }

    private void createTestUsersIfNotExist() {
        // Create admin user
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new RuntimeException("Admin role not found"));

            User admin = User.builder()
                .username("admin")
                .email("admin@example.com")
                .password(passwordEncoder.encode("admin123"))
                .firstName("Admin")
                .lastName("User")
                .enabled(true)
                .roles(Set.of(adminRole))
                .build();

            userRepository.save(admin);
            log.info("Created admin user: admin/admin123");
        }

        // Create regular user
        if (!userRepository.existsByUsername("user")) {
            Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new RuntimeException("User role not found"));

            User user = User.builder()
                .username("user")
                .email("user@example.com")
                .password(passwordEncoder.encode("user123"))
                .firstName("Test")
                .lastName("User")
                .enabled(true)
                .roles(Set.of(userRole))
                .build();

            userRepository.save(user);
            log.info("Created regular user: user/user123");
        }

        // Create moderator user
        if (!userRepository.existsByUsername("moderator")) {
            Role moderatorRole = roleRepository.findByName(RoleName.MODERATOR)
                .orElseThrow(() -> new RuntimeException("Moderator role not found"));

            User moderator = User.builder()
                .username("moderator")
                .email("moderator@example.com")
                .password(passwordEncoder.encode("moderator123"))
                .firstName("Moderator")
                .lastName("User")
                .enabled(true)
                .roles(Set.of(moderatorRole))
                .build();

            userRepository.save(moderator);
            log.info("Created moderator user: moderator/moderator123");
        }
    }

    private String getDescriptionForRole(RoleName roleName) {
        return switch (roleName) {
            case ADMIN -> "Administrator with full system access";
            case MODERATOR -> "Moderator with content management permissions";
            case USER -> "Regular user with basic permissions";
        };
    }
}