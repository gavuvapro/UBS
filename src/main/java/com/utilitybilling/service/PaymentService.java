package com.utilitybilling.service;

import com.utilitybilling.dto.request.PaymentRequest;
import com.utilitybilling.dto.response.PaymentResponse;
import com.utilitybilling.entity.*;
import com.utilitybilling.exception.BusinessRuleException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.repository.AppUserRepository;
import com.utilitybilling.repository.BillRepository;
import com.utilitybilling.repository.NotificationLogRepository;
import com.utilitybilling.repository.PaymentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service handling payment recording against utility bills.
 * Supports partial payments, updates bill balances, and triggers email/notification on full payment.
 */
@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final AppUserRepository appUserRepository;
    private final EmailService emailService;
    private final NotificationLogRepository notificationLogRepository;

    public PaymentService(PaymentRepository paymentRepository, BillRepository billRepository,
                         AppUserRepository appUserRepository, EmailService emailService,
                         NotificationLogRepository notificationLogRepository) {
        this.paymentRepository = paymentRepository;
        this.billRepository = billRepository;
        this.appUserRepository = appUserRepository;
        this.emailService = emailService;
        this.notificationLogRepository = notificationLogRepository;
    }

    @Transactional
    public PaymentResponse recordPayment(PaymentRequest request) {
        Bill bill = billRepository.findById(request.billId())
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", request.billId()));

        if (bill.getStatus() == Bill.BillStatus.PAID) {
            throw new BusinessRuleException("Bill is already fully paid.");
        }

        if (request.amountPaid().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Amount paid must be greater than zero.");
        }

        if (request.amountPaid().compareTo(bill.getOutstandingBalance()) > 0) {
            throw new BusinessRuleException("Amount paid cannot exceed outstanding balance. Outstanding balance: " + bill.getOutstandingBalance());
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication != null ? authentication.getName() : null;
        AppUser recordedBy = null;
        if (email != null) {
            recordedBy = appUserRepository.findByEmail(email).orElse(null);
        }

        String reference = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Payment payment = new Payment();
        payment.setBill(bill);
        payment.setAmountPaid(request.amountPaid());
        payment.setPaymentMethod(Payment.PaymentMethod.valueOf(request.paymentMethod()));
        payment.setPaymentDate(request.paymentDate());
        payment.setReference(reference);
        payment.setRecordedBy(recordedBy);

        paymentRepository.save(payment);

        // Update bill amounts
        BigDecimal newAmountPaid = bill.getAmountPaid().add(request.amountPaid()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal newOutstanding = bill.getTotalAmount().subtract(newAmountPaid).setScale(2, RoundingMode.HALF_UP);
        if (newOutstanding.compareTo(BigDecimal.ZERO) < 0) {
            newOutstanding = BigDecimal.ZERO;
        }

        bill.setAmountPaid(newAmountPaid);
        bill.setOutstandingBalance(newOutstanding);

        if (newOutstanding.compareTo(BigDecimal.ZERO) == 0) {
            bill.setStatus(Bill.BillStatus.PAID);
        }

        billRepository.save(bill);

        // Send payment confirmation email and log notification when fully paid
        if (bill.getStatus() == Bill.BillStatus.PAID) {
            Customer customer = bill.getCustomer();
            if (customer.getEmail() != null && !customer.getEmail().isBlank()) {
                String htmlBody = "<p>Dear " + customer.getFullNames() + ",</p>" +
                        "<p>Your payment for bill " + bill.getBillReference() + " has been received. Outstanding balance: 0 FRW.</p>";
                emailService.sendEmail(customer.getEmail(), "Payment Confirmation - " + bill.getBillReference(), htmlBody);
            }

            NotificationLog log = new NotificationLog();
            log.setCustomer(customer);
            log.setMessage("Dear " + customer.getFullNames() + ", Your payment for bill " + bill.getBillReference() + " has been received. Outstanding balance: 0 FRW.");
            log.setNotificationType(NotificationLog.NotificationType.PAYMENT_CONFIRMED);
            log.setCreatedAt(LocalDateTime.now());
            log.setSent(true);
            notificationLogRepository.save(log);
        }

        return mapToResponse(payment);
    }

    public Page<PaymentResponse> findAll(Pageable pageable) {
        return paymentRepository.findAll(pageable).map(this::mapToResponse);
    }

    public List<PaymentResponse> findByBillId(UUID billId) {
        return paymentRepository.findByBillId(billId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getBill().getId(),
                payment.getBill().getBillReference(),
                payment.getAmountPaid(),
                payment.getPaymentMethod().name(),
                payment.getPaymentDate(),
                payment.getReference(),
                payment.getRecordedBy() != null ? payment.getRecordedBy().getId() : null,
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
