package com.utilitybilling.repository;

import com.utilitybilling.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for AppUser entity lookups and existence checks by email.
 */
@Repository
public interface AppUserRepository extends JpaRepository<AppUser, UUID> {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
}
