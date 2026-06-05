package com.utilitybilling.repository;

import com.utilitybilling.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JPA repository for Payment entity lookups by associated bill ID.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByBillId(UUID billId);
}
