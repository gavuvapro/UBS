package com.utilitybilling.repository;

import com.utilitybilling.entity.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for Bill entity lookups by reference, customer, and meter reading.
 */
@Repository
public interface BillRepository extends JpaRepository<Bill, UUID> {
    Optional<Bill> findByBillReference(String billReference);
    Page<Bill> findByCustomerId(UUID customerId, Pageable pageable);
    List<Bill> findByCustomerId(UUID customerId);
    boolean existsByMeterReadingId(UUID meterReadingId);
}
