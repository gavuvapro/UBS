package com.utilitybilling.service;

import com.utilitybilling.dto.request.MeterRequest;
import com.utilitybilling.dto.response.MeterResponse;
import com.utilitybilling.entity.Customer;
import com.utilitybilling.entity.Meter;
import com.utilitybilling.exception.BusinessRuleException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.repository.CustomerRepository;
import com.utilitybilling.repository.MeterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Service managing meter lifecycle: creation, listing, update, and deactivation (status INACTIVE).
 * Enforces globally unique meter numbers and links meters to customers.
 */
@Service
public class MeterService {

    private final MeterRepository meterRepository;
    private final CustomerRepository customerRepository;

    public MeterService(MeterRepository meterRepository, CustomerRepository customerRepository) {
        this.meterRepository = meterRepository;
        this.customerRepository = customerRepository;
    }

    @Transactional
    public MeterResponse create(MeterRequest request) {
        if (meterRepository.existsByMeterNumber(request.meterNumber())) {
            throw new BusinessRuleException("Meter number already exists: " + request.meterNumber());
        }

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.customerId()));

        Meter meter = new Meter();
        meter.setMeterNumber(request.meterNumber());
        meter.setMeterType(Meter.MeterType.valueOf(request.meterType()));
        meter.setInstallationDate(request.installationDate());
        meter.setStatus(Meter.MeterStatus.ACTIVE);
        meter.setCustomer(customer);

        meterRepository.save(meter);
        return mapToResponse(meter);
    }

    public Page<MeterResponse> findAll(Pageable pageable) {
        return meterRepository.findAll(pageable).map(this::mapToResponse);
    }

    public MeterResponse findById(UUID id) {
        Meter meter = meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter", "id", id));
        return mapToResponse(meter);
    }

    @Transactional
    public MeterResponse update(UUID id, MeterRequest request) {
        Meter meter = meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter", "id", id));

        if (!meter.getMeterNumber().equals(request.meterNumber()) && meterRepository.existsByMeterNumber(request.meterNumber())) {
            throw new BusinessRuleException("Meter number already exists: " + request.meterNumber());
        }

        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.customerId()));

        meter.setMeterNumber(request.meterNumber());
        meter.setMeterType(Meter.MeterType.valueOf(request.meterType()));
        meter.setInstallationDate(request.installationDate());
        meter.setCustomer(customer);
        if (request.status() != null) {
            meter.setStatus(Meter.MeterStatus.valueOf(request.status()));
        }

        meterRepository.save(meter);
        return mapToResponse(meter);
    }

    @Transactional
    public void delete(UUID id) {
        Meter meter = meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter", "id", id));
        meter.setStatus(Meter.MeterStatus.INACTIVE);
        meterRepository.save(meter);
    }

    private MeterResponse mapToResponse(Meter meter) {
        return new MeterResponse(
                meter.getId(),
                meter.getMeterNumber(),
                meter.getMeterType().name(),
                meter.getInstallationDate(),
                meter.getStatus().name(),
                meter.getCustomer() != null ? meter.getCustomer().getId() : null,
                meter.getCustomer() != null ? meter.getCustomer().getFullNames() : null,
                meter.getCreatedAt(),
                meter.getUpdatedAt()
        );
    }

    public Meter getMeterEntity(UUID id) {
        return meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter", "id", id));
    }
}
