package com.gm2dev.demo_spring.service.user;

import com.gm2dev.demo_spring.entity.user.User;
import com.gm2dev.demo_spring.exception.ResourceNotFoundException;
import com.gm2dev.demo_spring.repository.user.UserRepository;
import com.gm2dev.demo_spring.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public User getCurrentUser(UserPrincipal currentUser) {
        return getUserById(currentUser.getId());
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAllWithRoles(pageable);
    }

    @Transactional(readOnly = true)
    public Boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public Boolean isEmailAvailable(String email) {
        return !userRepository.existsByEmail(email);
    }

    @Transactional
    public User createUser(User user) {
        log.info("Creating user: {}", user.getUsername());
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(User user) {
        log.info("Updating user: {}", user.getUsername());
        return userRepository.save(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        log.info("Deleting user: {}", user.getUsername());
        userRepository.delete(user);
    }
}