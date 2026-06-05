package com.utilitybilling.service;

import com.utilitybilling.dto.request.MeterReadingRequest;
import com.utilitybilling.dto.response.MeterReadingResponse;
import com.utilitybilling.entity.Meter;
import com.utilitybilling.entity.MeterReading;
import com.utilitybilling.exception.BusinessRuleException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.repository.MeterReadingRepository;
import com.utilitybilling.repository.MeterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service for capturing meter readings with business rule enforcement.
 * Rules: active meter only, one reading per month/year, strictly increasing current reading.
 */
@Service
public class MeterReadingService {

    private final MeterReadingRepository meterReadingRepository;
    private final MeterRepository meterRepository;

    public MeterReadingService(MeterReadingRepository meterReadingRepository, MeterRepository meterRepository) {
        this.meterReadingRepository = meterReadingRepository;
        this.meterRepository = meterRepository;
    }

    @Transactional
    public MeterReadingResponse create(MeterReadingRequest request) {
        Meter meter = meterRepository.findById(request.meterId())
                .orElseThrow(() -> new ResourceNotFoundException("Meter", "id", request.meterId()));

        if (meter.getStatus() != Meter.MeterStatus.ACTIVE) {
            throw new BusinessRuleException("Meter is not active. Readings can only be captured for active meters.");
        }

        if (meterReadingRepository.existsByMeterIdAndReadingMonthAndReadingYear(
                meter.getId(), request.readingMonth(), request.readingYear())) {
            throw new BusinessRuleException("A reading for this meter already exists for the specified month and year.");
        }

        if (request.currentReading().compareTo(request.previousReading()) <= 0) {
            throw new BusinessRuleException("Current reading must be strictly greater than previous reading.");
        }

        BigDecimal consumption = request.currentReading().subtract(request.previousReading());

        MeterReading reading = new MeterReading();
        reading.setMeter(meter);
        reading.setPreviousReading(request.previousReading());
        reading.setCurrentReading(request.currentReading());
        reading.setReadingDate(request.readingDate());
        reading.setConsumption(consumption);
        reading.setReadingMonth(request.readingMonth());
        reading.setReadingYear(request.readingYear());

        meterReadingRepository.save(reading);
        return mapToResponse(reading);
    }

    public Page<MeterReadingResponse> findAll(Pageable pageable) {
        return meterReadingRepository.findAll(pageable).map(this::mapToResponse);
    }

    public MeterReadingResponse findById(UUID id) {
        MeterReading reading = meterReadingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MeterReading", "id", id));
        return mapToResponse(reading);
    }

    private MeterReadingResponse mapToResponse(MeterReading reading) {
        return new MeterReadingResponse(
                reading.getId(),
                reading.getMeter().getId(),
                reading.getMeter().getMeterNumber(),
                reading.getPreviousReading(),
                reading.getCurrentReading(),
                reading.getConsumption(),
                reading.getReadingDate(),
                reading.getReadingMonth(),
                reading.getReadingYear(),
                reading.getCreatedAt(),
                reading.getUpdatedAt()
        );
    }

    public MeterReading getMeterReadingEntity(UUID id) {
        return meterReadingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MeterReading", "id", id));
    }
}
