package com.utilitybilling.service;

import com.utilitybilling.dto.response.BillResponse;
import com.utilitybilling.entity.*;
import com.utilitybilling.exception.BusinessRuleException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.repository.*;
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
 * Core billing engine service that generates bills from meter readings.
 * Calculates consumption charges (flat or tiered), applies service charges, taxes, and generates bill references.
 * Triggers email notifications and notification logs upon bill generation.
 */
@Service
public class BillingService {

    private final BillRepository billRepository;
    private final MeterReadingRepository meterReadingRepository;
    private final TariffService tariffService;
    private final CustomerRepository customerRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final EmailService emailService;
    private final MeterRepository meterRepository;

    public BillingService(BillRepository billRepository, MeterReadingRepository meterReadingRepository,
                         TariffService tariffService, CustomerRepository customerRepository,
                         NotificationLogRepository notificationLogRepository, EmailService emailService,
                         MeterRepository meterRepository) {
        this.billRepository = billRepository;
        this.meterReadingRepository = meterReadingRepository;
        this.tariffService = tariffService;
        this.customerRepository = customerRepository;
        this.notificationLogRepository = notificationLogRepository;
        this.emailService = emailService;
        this.meterRepository = meterRepository;
    }

    @Transactional
    public BillResponse generateBill(UUID meterReadingId) {
        MeterReading reading = meterReadingRepository.findById(meterReadingId)
                .orElseThrow(() -> new ResourceNotFoundException("MeterReading", "id", meterReadingId));

        Customer customer = reading.getMeter().getCustomer();
        if (customer == null) {
            throw new BusinessRuleException("Meter is not associated with any customer.");
        }
        if (customer.getStatus() != Customer.CustomerStatus.ACTIVE) {
            throw new BusinessRuleException("Customer is inactive. Bills cannot be generated for inactive customers.");
        }

        Meter meter = reading.getMeter();

        if (billRepository.existsByMeterReadingId(meterReadingId)) {
            throw new BusinessRuleException("Bill already generated for this meter reading.");
        }

        Tariff tariff = tariffService.getActiveTariff(meter.getMeterType());
        ServiceCharge serviceCharge = tariffService.getActiveServiceCharge(meter.getMeterType());
        TaxConfig taxConfig = tariffService.getActiveTaxConfig();

        BigDecimal consumption = reading.getConsumption();
        BigDecimal consumptionCharge = calculateConsumptionCharge(consumption, tariff);
        BigDecimal serviceChargeAmount = serviceCharge != null ? serviceCharge.getAmount() : BigDecimal.ZERO;
        BigDecimal taxableBase = consumptionCharge.add(serviceChargeAmount);
        BigDecimal taxAmount = taxableBase.multiply(taxConfig.getRate()).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = consumptionCharge.add(serviceChargeAmount).add(taxAmount).setScale(2, RoundingMode.HALF_UP);

        String billReference = "BILL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Bill bill = new Bill();
        bill.setBillReference(billReference);
        bill.setCustomer(customer);
        bill.setMeter(meter);
        bill.setMeterReading(reading);
        bill.setBillingMonth(reading.getReadingMonth());
        bill.setBillingYear(reading.getReadingYear());
        bill.setConsumption(consumption);
        bill.setConsumptionCharge(consumptionCharge);
        bill.setServiceCharge(serviceChargeAmount);
        bill.setTaxAmount(taxAmount);
        bill.setPenaltyAmount(BigDecimal.ZERO);
        bill.setTotalAmount(totalAmount);
        bill.setAmountPaid(BigDecimal.ZERO);
        bill.setOutstandingBalance(totalAmount);
        bill.setStatus(Bill.BillStatus.PENDING);
        bill.setGeneratedAt(LocalDateTime.now());

        billRepository.save(bill);

        // Send email notification
        if (customer.getEmail() != null && !customer.getEmail().isBlank()) {
            String monthYear = reading.getReadingMonth() + "/" + reading.getReadingYear();
            String htmlBody = "<p>Dear " + customer.getFullNames() + ",</p>" +
                    "<p>Your " + monthYear + " utility bill of " + totalAmount + " FRW has been successfully processed.</p>";
            emailService.sendEmail(customer.getEmail(), "Your Utility Bill - " + billReference, htmlBody);
        }

        // Insert notification log via Java service layer
        NotificationLog log = new NotificationLog();
        log.setCustomer(customer);
        log.setMessage("Dear " + customer.getFullNames() + ", Your " + reading.getReadingMonth() + "/" + reading.getReadingYear() +
                " utility bill of " + totalAmount + " FRW has been successfully processed.");
        log.setNotificationType(NotificationLog.NotificationType.BILL_GENERATED);
        log.setCreatedAt(LocalDateTime.now());
        log.setSent(true);
        notificationLogRepository.save(log);

        return mapToResponse(bill);
    }

    /**
     * Calculates consumption charge based on tariff type: FLAT or TIER.
     * For tier tariffs, computes consumption across each band with partial band support.
     */
    private BigDecimal calculateConsumptionCharge(BigDecimal consumption, Tariff tariff) {
        if (tariff.getTariffType() == Tariff.TariffType.FLAT) {
            return consumption.multiply(tariff.getUnitPrice()).setScale(2, RoundingMode.HALF_UP);
        } else {
            BigDecimal totalCharge = BigDecimal.ZERO;
            List<TariffTier> tiers = tariff.getTiers();
            if (tiers == null || tiers.isEmpty()) {
                throw new BusinessRuleException("TIER tariff has no configured tiers.");
            }

            BigDecimal remaining = consumption;
            List<TariffTier> sortedTiers = tiers.stream()
                    .sorted((a, b) -> a.getMinUnits().compareTo(b.getMinUnits()))
                    .collect(Collectors.toList());

            for (TariffTier tier : sortedTiers) {
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }
                BigDecimal tierRange = tier.getMaxUnits() != null
                        ? tier.getMaxUnits().subtract(tier.getMinUnits())
                        : remaining;
                BigDecimal unitsInTier = remaining.min(tierRange.max(BigDecimal.ZERO));
                if (unitsInTier.compareTo(BigDecimal.ZERO) > 0) {
                    totalCharge = totalCharge.add(unitsInTier.multiply(tier.getPricePerUnit()));
                    remaining = remaining.subtract(unitsInTier);
                }
            }

            if (remaining.compareTo(BigDecimal.ZERO) > 0) {
                TariffTier lastTier = sortedTiers.get(sortedTiers.size() - 1);
                totalCharge = totalCharge.add(remaining.multiply(lastTier.getPricePerUnit()));
            }

            return totalCharge.setScale(2, RoundingMode.HALF_UP);
        }
    }

    public Page<BillResponse> findAll(Pageable pageable) {
        return billRepository.findAll(pageable).map(this::mapToResponse);
    }

    public BillResponse findById(UUID id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", id));
        return mapToResponse(bill);
    }

    @Transactional
    public BillResponse approveBill(UUID id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", id));

        if (bill.getStatus() != Bill.BillStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING bills can be approved.");
        }

        bill.setStatus(Bill.BillStatus.APPROVED);
        bill.setApprovedAt(LocalDateTime.now());
        // approvedBy is left nullable for simplicity; could be populated from SecurityContext

        billRepository.save(bill);
        return mapToResponse(bill);
    }

    public Page<BillResponse> findByCustomerId(UUID customerId, Pageable pageable) {
        return billRepository.findByCustomerId(customerId, pageable).map(this::mapToResponse);
    }

    private BillResponse mapToResponse(Bill bill) {
        return new BillResponse(
                bill.getId(),
                bill.getBillReference(),
                bill.getCustomer().getId(),
                bill.getCustomer().getFullNames(),
                bill.getMeter().getId(),
                bill.getMeter().getMeterNumber(),
                bill.getMeterReading() != null ? bill.getMeterReading().getId() : null,
                bill.getBillingMonth(),
                bill.getBillingYear(),
                bill.getConsumption(),
                bill.getConsumptionCharge(),
                bill.getServiceCharge(),
                bill.getTaxAmount(),
                bill.getPenaltyAmount(),
                bill.getTotalAmount(),
                bill.getAmountPaid(),
                bill.getOutstandingBalance(),
                bill.getStatus().name(),
                bill.getGeneratedAt(),
                bill.getApprovedAt(),
                bill.getApprovedBy() != null ? bill.getApprovedBy().getId() : null,
                bill.getCreatedAt(),
                bill.getUpdatedAt()
        );
    }
}
