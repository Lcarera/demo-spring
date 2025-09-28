package com.gm2dev.demo_spring.repository.user;

import com.gm2dev.demo_spring.entity.user.Role;
import com.gm2dev.demo_spring.entity.user.Role.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);

    Boolean existsByName(RoleName name);
}