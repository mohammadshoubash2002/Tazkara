package com.mohammad.identity_service.repository;

import com.mohammad.identity_service.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByOrganizationName(String organization_name);
    List<AppUser> findByOrganizationName(String organization_name);
}
