package com.utilitybilling.service;

import com.utilitybilling.entity.AppUser;
import com.utilitybilling.entity.Bill;
import com.utilitybilling.entity.Customer;
import com.utilitybilling.entity.Meter;
import com.utilitybilling.entity.MeterReading;
import com.utilitybilling.entity.NotificationLog;
import com.utilitybilling.entity.PenaltyConfig;
import com.utilitybilling.entity.Payment;
import com.utilitybilling.entity.Role;
import com.utilitybilling.entity.ServiceCharge;
import com.utilitybilling.entity.Tariff;
import com.utilitybilling.entity.TariffTier;
import com.utilitybilling.entity.TaxConfig;
import com.utilitybilling.repository.AppUserRepository;
import com.utilitybilling.repository.BillRepository;
import com.utilitybilling.repository.CustomerRepository;
import com.utilitybilling.repository.MeterReadingRepository;
import com.utilitybilling.repository.MeterRepository;
import com.utilitybilling.repository.NotificationLogRepository;
import com.utilitybilling.repository.PenaltyConfigRepository;
import com.utilitybilling.repository.PaymentRepository;
import com.utilitybilling.repository.RoleRepository;
import com.utilitybilling.repository.ServiceChargeRepository;
import com.utilitybilling.repository.TariffRepository;
import com.utilitybilling.repository.TariffTierRepository;
import com.utilitybilling.repository.TaxConfigRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Seed data initializer that runs on application startup.
 * Ensures all four system roles exist and creates a default ADMIN user for initial access.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final CustomerRepository customerRepository;
    private final MeterRepository meterRepository;
    private final MeterReadingRepository meterReadingRepository;
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;
    private final NotificationLogRepository notificationLogRepository;
    private final PenaltyConfigRepository penaltyConfigRepository;
    private final ServiceChargeRepository serviceChargeRepository;
    private final TariffRepository tariffRepository;
    private final TariffTierRepository tariffTierRepository;
    private final TaxConfigRepository taxConfigRepository;

    public DataInitializer(RoleRepository roleRepository,
                           AppUserRepository appUserRepository,
                           PasswordEncoder passwordEncoder,
                           CustomerRepository customerRepository,
                           MeterRepository meterRepository,
                           MeterReadingRepository meterReadingRepository,
                           BillRepository billRepository,
                           PaymentRepository paymentRepository,
                           NotificationLogRepository notificationLogRepository,
                           PenaltyConfigRepository penaltyConfigRepository,
                           ServiceChargeRepository serviceChargeRepository,
                           TariffRepository tariffRepository,
                           TariffTierRepository tariffTierRepository,
                           TaxConfigRepository taxConfigRepository) {
        this.roleRepository = roleRepository;
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.customerRepository = customerRepository;
        this.meterRepository = meterRepository;
        this.meterReadingRepository = meterReadingRepository;
        this.billRepository = billRepository;
        this.paymentRepository = paymentRepository;
        this.notificationLogRepository = notificationLogRepository;
        this.penaltyConfigRepository = penaltyConfigRepository;
        this.serviceChargeRepository = serviceChargeRepository;
        this.tariffRepository = tariffRepository;
        this.tariffTierRepository = tariffTierRepository;
        this.taxConfigRepository = taxConfigRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        Role adminRole = createRoleIfNotExists("ROLE_ADMIN");
        Role operatorRole = createRoleIfNotExists("ROLE_OPERATOR");
        Role financeRole = createRoleIfNotExists("ROLE_FINANCE");
        Role customerRole = createRoleIfNotExists("ROLE_CUSTOMER");

        if (!appUserRepository.existsByEmail("admin@wasac.rw")) {
            AppUser admin = new AppUser();
            admin.setFullNames("System Administrator");
            admin.setEmail("admin@wasac.rw");
            admin.setPhoneNumber("+250700000001");
            admin.setPassword(passwordEncoder.encode("Admin123"));
            admin.setStatus(AppUser.UserStatus.ACTIVE);

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            roles.add(operatorRole);
            roles.add(financeRole);
            admin.setRoles(roles);

            appUserRepository.save(admin);
        }

        // Seed additional test data (10 records per table)
        seedTestData();
    }

    /**
     * Populate the database with sample data for testing.
     * Creates 10 customers, each with a meter, reading, bill, payment, notification, etc.
     */
    private void seedTestData() {
        // Avoid reseeding if data already exists
        if (customerRepository.count() >= 10) {
            return;
        }

        // Retrieve admin user for audit fields (e.g., payment recordedBy)
        Optional<AppUser> adminOpt = appUserRepository.findByEmail("admin@wasac.rw");
        AppUser admin = adminOpt.orElse(null);

        for (int i = 1; i <= 10; i++) {
            String idx = String.format("%04d", i);

            // Customer
            Customer customer = new Customer();
            customer.setFullNames("Customer " + i);
            customer.setNationalId("NID-" + idx);
            customer.setEmail("customer" + i + "@example.com");
            customer.setPhoneNumber("+2507000" + (1000 + i));
            customer.setAddress("Address " + i);
            customer.setStatus(Customer.CustomerStatus.ACTIVE);
            customerRepository.save(customer);

            // Meter
            Meter meter = new Meter();
            meter.setMeterNumber("MTR-" + idx);
            meter.setMeterType(i % 2 == 0 ? Meter.MeterType.ELECTRICITY : Meter.MeterType.WATER);
            meter.setInstallationDate(LocalDate.now().minusMonths(6));
            meter.setStatus(Meter.MeterStatus.ACTIVE);
            meter.setCustomer(customer);
            meterRepository.save(meter);

            // MeterReading
            MeterReading reading = new MeterReading();
            reading.setMeter(meter);
            BigDecimal prev = BigDecimal.valueOf(100L * i);
            BigDecimal curr = prev.add(BigDecimal.valueOf(10L * i));
            reading.setPreviousReading(prev);
            reading.setCurrentReading(curr);
            reading.setReadingDate(LocalDate.now().minusDays(30));
            reading.setConsumption(curr.subtract(prev));
            reading.setReadingMonth(reading.getReadingDate().getMonthValue());
            reading.setReadingYear(reading.getReadingDate().getYear());
            meterReadingRepository.save(reading);

            // Bill
            Bill bill = new Bill();
            bill.setBillReference("BILL-" + idx);
            bill.setCustomer(customer);
            bill.setMeter(meter);
            bill.setMeterReading(reading);
            bill.setBillingMonth(reading.getReadingMonth());
            bill.setBillingYear(reading.getReadingYear());
            bill.setConsumption(reading.getConsumption());
            // Simple consumption charge calculation
            BigDecimal unitPrice = BigDecimal.valueOf(2.5 + i);
            BigDecimal consumptionCharge = reading.getConsumption().multiply(unitPrice);
            bill.setConsumptionCharge(consumptionCharge);
            bill.setServiceCharge(BigDecimal.ZERO);
            bill.setTaxAmount(BigDecimal.ZERO);
            bill.setPenaltyAmount(BigDecimal.ZERO);
            bill.setTotalAmount(consumptionCharge);
            bill.setAmountPaid(BigDecimal.ZERO);
            bill.setOutstandingBalance(consumptionCharge);
            bill.setStatus(Bill.BillStatus.PENDING);
            bill.setGeneratedAt(LocalDateTime.now());
            billRepository.save(bill);

            // Payment (partial)
            Payment payment = new Payment();
            payment.setBill(bill);
            payment.setAmountPaid(consumptionCharge.divide(BigDecimal.valueOf(2)));
            payment.setPaymentMethod(Payment.PaymentMethod.CASH);
            payment.setPaymentDate(LocalDate.now());
            payment.setReference("PAY-" + idx);
            payment.setRecordedBy(admin);
            paymentRepository.save(payment);

            // NotificationLog
            NotificationLog notif = new NotificationLog();
            notif.setCustomer(customer);
            notif.setMessage("Bill generated: " + bill.getBillReference());
            notif.setNotificationType(NotificationLog.NotificationType.BILL_GENERATED);
            notif.setCreatedAt(LocalDateTime.now());
            notif.setSent(true);
            notificationLogRepository.save(notif);

            // PenaltyConfig
            PenaltyConfig penalty = new PenaltyConfig();
            penalty.setPenaltyType(i % 2 == 0 ? PenaltyConfig.PenaltyType.FIXED : PenaltyConfig.PenaltyType.PERCENTAGE);
            penalty.setValue(BigDecimal.valueOf(i));
            penalty.setGracePeriodDays(5);
            penalty.setEffectiveDate(LocalDate.now().minusDays(30));
            penalty.setStatus(PenaltyConfig.PenaltyStatus.ACTIVE);
            penaltyConfigRepository.save(penalty);

            // ServiceCharge
            ServiceCharge serviceCharge = new ServiceCharge();
            serviceCharge.setName("Service Charge " + i);
            serviceCharge.setAmount(BigDecimal.valueOf(1 + i));
            serviceCharge.setMeterType(meter.getMeterType());
            serviceCharge.setEffectiveDate(LocalDate.now().minusDays(30));
            serviceCharge.setStatus(ServiceCharge.ChargeStatus.ACTIVE);
            serviceChargeRepository.save(serviceCharge);

            // Tariff (alternate flat/tier)
            Tariff tariff = new Tariff();
            tariff.setMeterType(meter.getMeterType());
            if (i % 2 == 0) {
                tariff.setTariffType(Tariff.TariffType.TIER);
                tariff.setUnitPrice(null);
            } else {
                tariff.setTariffType(Tariff.TariffType.FLAT);
                tariff.setUnitPrice(BigDecimal.valueOf(0.5 + i));
            }
            tariff.setEffectiveDate(LocalDate.now().minusDays(30));
            tariff.setVersion(1);
            tariff.setStatus(Tariff.TariffStatus.ACTIVE);
            tariff.setDescription("Sample tariff " + i);
            tariffRepository.save(tariff);

            // TariffTier for tiered tariffs
            if (tariff.getTariffType() == Tariff.TariffType.TIER) {
                TariffTier tier = new TariffTier();
                tier.setTariff(tariff);
                tier.setMinUnits(BigDecimal.ZERO);
                tier.setMaxUnits(BigDecimal.valueOf(100));
                tier.setPricePerUnit(BigDecimal.valueOf(1 + i));
                tariffTierRepository.save(tier);
            }

            // TaxConfig
            TaxConfig tax = new TaxConfig();
            tax.setTaxName("VAT " + i);
            tax.setRate(BigDecimal.valueOf(0.18));
            tax.setEffectiveDate(LocalDate.now().minusDays(30));
            tax.setStatus(TaxConfig.TaxStatus.ACTIVE);
            taxConfigRepository.save(tax);
        }
    }

    private Role createRoleIfNotExists(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseGet(() -> roleRepository.save(new Role(roleName)));
    }
}
