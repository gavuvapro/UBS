package com.utilitybilling.controller;

import com.utilitybilling.dto.request.PenaltyConfigRequest;
import com.utilitybilling.dto.request.ServiceChargeRequest;
import com.utilitybilling.dto.request.TariffRequest;
import com.utilitybilling.dto.request.TariffTierRequest;
import com.utilitybilling.dto.request.TaxConfigRequest;
import com.utilitybilling.dto.response.TariffResponse;
import com.utilitybilling.entity.PenaltyConfig;
import com.utilitybilling.entity.ServiceCharge;
import com.utilitybilling.entity.TaxConfig;
import com.utilitybilling.service.TariffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Admin endpoints for tariff, tax, service charge, and penalty configuration.
 * Supports flat and tier tariffs with version management.
 */
@RestController
@RequestMapping("/api")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tariff & Configuration", description = "Admin operations for pricing, taxes, service charges, and penalties")
public class TariffController {

    private final TariffService tariffService;

    public TariffController(TariffService tariffService) {
        this.tariffService = tariffService;
    }

    // Tariff endpoints

    @Operation(summary = "Create tariff", description = "Creates a new tariff with auto-incremented version per meter type. Starts as INACTIVE.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tariff created",
                     content = @Content(schema = @Schema(implementation = TariffResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/tariffs")
    public ResponseEntity<TariffResponse> createTariff(@Valid @RequestBody TariffRequest request) {
        return ResponseEntity.ok(tariffService.createTariff(request));
    }

    @Operation(summary = "List all tariffs", description = "Paginated list of all tariffs with tier details")
    @ApiResponse(responseCode = "200", description = "List of tariffs returned")
    @GetMapping("/tariffs")
    public ResponseEntity<Page<TariffResponse>> listTariffs(Pageable pageable) {
        return ResponseEntity.ok(tariffService.findAllTariffs(pageable));
    }

    @Operation(summary = "Activate tariff", description = "Activates a tariff and deactivates the previous active tariff for the same meter type.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tariff activated",
                     content = @Content(schema = @Schema(implementation = TariffResponse.class))),
        @ApiResponse(responseCode = "404", description = "Tariff not found")
    })
    @PutMapping("/tariffs/{id}/activate")
    public ResponseEntity<TariffResponse> activateTariff(
            @Parameter(description = "Tariff UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(tariffService.activateTariff(id));
    }

    @Operation(summary = "Add tariff tier", description = "Adds a pricing band to a TIER-type tariff.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tier added",
                     content = @Content(schema = @Schema(implementation = TariffResponse.class))),
        @ApiResponse(responseCode = "400", description = "Tariff is not TIER type"),
        @ApiResponse(responseCode = "404", description = "Tariff not found")
    })
    @PostMapping("/tariffs/{id}/tiers")
    public ResponseEntity<TariffResponse> addTier(
            @Parameter(description = "Tariff UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody TariffTierRequest request) {
        return ResponseEntity.ok(tariffService.addTier(id, request));
    }

    // Service Charge endpoints

    @Operation(summary = "Create service charge", description = "Creates a fixed service charge configuration for a meter type.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Service charge created",
                     content = @Content(schema = @Schema(implementation = ServiceCharge.class))),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/service-charges")
    public ResponseEntity<ServiceCharge> createServiceCharge(@Valid @RequestBody ServiceChargeRequest request) {
        return ResponseEntity.ok(tariffService.createServiceCharge(request));
    }

    // Tax Config endpoints

    @Operation(summary = "Create tax config", description = "Creates a tax rule (e.g., VAT 18%). Only one active tax is expected by billing.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Tax config created",
                     content = @Content(schema = @Schema(implementation = TaxConfig.class))),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/tax-configs")
    public ResponseEntity<TaxConfig> createTaxConfig(@Valid @RequestBody TaxConfigRequest request) {
        return ResponseEntity.ok(tariffService.createTaxConfig(request));
    }

    // Penalty Config endpoints

    @Operation(summary = "Create penalty config", description = "Creates a penalty rule (fixed amount or percentage) with grace period.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Penalty config created",
                     content = @Content(schema = @Schema(implementation = PenaltyConfig.class))),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping("/penalty-configs")
    public ResponseEntity<PenaltyConfig> createPenaltyConfig(@Valid @RequestBody PenaltyConfigRequest request) {
        return ResponseEntity.ok(tariffService.createPenaltyConfig(request));
    }
}
