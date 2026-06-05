package com.utilitybilling.repository;

import com.utilitybilling.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for Customer entity lookups by national ID and linked user ID.
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByNationalId(String nationalId);
    boolean existsByNationalId(String nationalId);
    Optional<Customer> findByUserId(UUID userId);
}
