package com.utilitybilling.service;

import com.utilitybilling.dto.request.PenaltyConfigRequest;
import com.utilitybilling.dto.request.ServiceChargeRequest;
import com.utilitybilling.dto.request.TariffRequest;
import com.utilitybilling.dto.request.TariffTierRequest;
import com.utilitybilling.dto.request.TaxConfigRequest;
import com.utilitybilling.dto.response.TariffResponse;
import com.utilitybilling.dto.response.TariffTierResponse;
import com.utilitybilling.entity.Meter;
import com.utilitybilling.entity.ServiceCharge;
import com.utilitybilling.entity.Tariff;
import com.utilitybilling.entity.TariffTier;
import com.utilitybilling.entity.TaxConfig;
import com.utilitybilling.entity.PenaltyConfig;
import com.utilitybilling.exception.BusinessRuleException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.repository.ServiceChargeRepository;
import com.utilitybilling.repository.TariffRepository;
import com.utilitybilling.repository.TariffTierRepository;
import com.utilitybilling.repository.TaxConfigRepository;
import com.utilitybilling.repository.PenaltyConfigRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service managing tariff configuration, tier pricing, service charges, tax rules, and penalties.
 * Enforces version auto-increment on new tariffs and only one active tariff per meter type.
 */
@Service
public class TariffService {

    private final TariffRepository tariffRepository;
    private final TariffTierRepository tariffTierRepository;
    private final ServiceChargeRepository serviceChargeRepository;
    private final TaxConfigRepository taxConfigRepository;
    private final PenaltyConfigRepository penaltyConfigRepository;

    public TariffService(TariffRepository tariffRepository, TariffTierRepository tariffTierRepository,
                         ServiceChargeRepository serviceChargeRepository, TaxConfigRepository taxConfigRepository,
                         PenaltyConfigRepository penaltyConfigRepository) {
        this.tariffRepository = tariffRepository;
        this.tariffTierRepository = tariffTierRepository;
        this.serviceChargeRepository = serviceChargeRepository;
        this.taxConfigRepository = taxConfigRepository;
        this.penaltyConfigRepository = penaltyConfigRepository;
    }

    @Transactional
    public TariffResponse createTariff(TariffRequest request) {
        Tariff tariff = new Tariff();
        tariff.setMeterType(Meter.MeterType.valueOf(request.meterType()));
        tariff.setTariffType(Tariff.TariffType.valueOf(request.tariffType()));
        tariff.setUnitPrice(request.unitPrice());
        tariff.setEffectiveDate(request.effectiveDate());
        tariff.setDescription(request.description());
        tariff.setStatus(Tariff.TariffStatus.INACTIVE);

        Integer maxVersion = tariffRepository.findMaxVersionByMeterType(tariff.getMeterType()).orElse(0);
        tariff.setVersion(maxVersion + 1);

        tariffRepository.save(tariff);
        return mapToResponse(tariff);
    }

    public Page<TariffResponse> findAllTariffs(Pageable pageable) {
        return tariffRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional
    public TariffResponse activateTariff(UUID id) {
        Tariff tariff = tariffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tariff", "id", id));

        tariffRepository.findByMeterTypeAndStatus(tariff.getMeterType(), Tariff.TariffStatus.ACTIVE)
                .ifPresent(activeTariff -> {
                    activeTariff.setStatus(Tariff.TariffStatus.INACTIVE);
                    tariffRepository.save(activeTariff);
                });

        tariff.setStatus(Tariff.TariffStatus.ACTIVE);
        tariffRepository.save(tariff);
        return mapToResponse(tariff);
    }

    @Transactional
    public TariffResponse addTier(UUID tariffId, TariffTierRequest request) {
        Tariff tariff = tariffRepository.findById(tariffId)
                .orElseThrow(() -> new ResourceNotFoundException("Tariff", "id", tariffId));

        if (tariff.getTariffType() != Tariff.TariffType.TIER) {
            throw new BusinessRuleException("Tiers can only be added to TIER type tariffs.");
        }

        TariffTier tier = new TariffTier();
        tier.setTariff(tariff);
        tier.setMinUnits(request.minUnits());
        tier.setMaxUnits(request.maxUnits());
        tier.setPricePerUnit(request.pricePerUnit());
        tariffTierRepository.save(tier);

        return mapToResponse(tariff);
    }

    @Transactional
    public ServiceCharge createServiceCharge(ServiceChargeRequest request) {
        ServiceCharge charge = new ServiceCharge();
        charge.setName(request.name());
        charge.setAmount(request.amount());
        charge.setMeterType(Meter.MeterType.valueOf(request.meterType()));
        charge.setEffectiveDate(request.effectiveDate());
        charge.setStatus(ServiceCharge.ChargeStatus.ACTIVE);
        return serviceChargeRepository.save(charge);
    }

    @Transactional
    public TaxConfig createTaxConfig(TaxConfigRequest request) {
        TaxConfig tax = new TaxConfig();
        tax.setTaxName(request.taxName());
        tax.setRate(request.rate());
        tax.setEffectiveDate(request.effectiveDate());
        tax.setStatus(TaxConfig.TaxStatus.ACTIVE);
        return taxConfigRepository.save(tax);
    }

    @Transactional
    public PenaltyConfig createPenaltyConfig(PenaltyConfigRequest request) {
        PenaltyConfig penalty = new PenaltyConfig();
        penalty.setPenaltyType(PenaltyConfig.PenaltyType.valueOf(request.penaltyType()));
        penalty.setValue(request.value());
        penalty.setGracePeriodDays(request.gracePeriodDays());
        penalty.setEffectiveDate(request.effectiveDate());
        penalty.setStatus(PenaltyConfig.PenaltyStatus.ACTIVE);
        return penaltyConfigRepository.save(penalty);
    }

    private TariffResponse mapToResponse(Tariff tariff) {
        List<TariffTierResponse> tiers = tariff.getTiers().stream()
                .map(t -> new TariffTierResponse(
                        t.getId(),
                        t.getTariff().getId(),
                        t.getMinUnits(),
                        t.getMaxUnits(),
                        t.getPricePerUnit()
                )).collect(Collectors.toList());

        return new TariffResponse(
                tariff.getId(),
                tariff.getMeterType().name(),
                tariff.getTariffType().name(),
                tariff.getUnitPrice(),
                tariff.getEffectiveDate(),
                tariff.getVersion(),
                tariff.getStatus().name(),
                tariff.getDescription(),
                tiers,
                tariff.getCreatedAt(),
                tariff.getUpdatedAt()
        );
    }

    public Tariff getActiveTariff(Meter.MeterType meterType) {
        return tariffRepository.findByMeterTypeAndStatus(meterType, Tariff.TariffStatus.ACTIVE)
                .orElseThrow(() -> new BusinessRuleException("No active tariff found for meter type: " + meterType));
    }

    public ServiceCharge getActiveServiceCharge(Meter.MeterType meterType) {
        return serviceChargeRepository.findByMeterTypeAndStatus(meterType, ServiceCharge.ChargeStatus.ACTIVE)
                .orElseThrow(() -> new BusinessRuleException("No active service charge found for meter type: " + meterType));
    }

    public TaxConfig getActiveTaxConfig() {
        return taxConfigRepository.findByStatus(TaxConfig.TaxStatus.ACTIVE)
                .orElseThrow(() -> new BusinessRuleException("No active tax configuration found."));
    }
}
