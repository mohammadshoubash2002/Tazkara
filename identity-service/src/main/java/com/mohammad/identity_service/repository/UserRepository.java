package com.mohammad.identity_service.repository;

import com.mohammad.identity_service.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    AppUser findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByOrganization_name(String organization_name);
    List<AppUser> findByOrganization_name(String organization_name);
}
