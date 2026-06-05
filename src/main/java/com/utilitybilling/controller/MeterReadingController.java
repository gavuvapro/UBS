package com.utilitybilling.controller;

import com.utilitybilling.dto.request.MeterReadingRequest;
import com.utilitybilling.dto.response.MeterReadingResponse;
import com.utilitybilling.service.MeterReadingService;
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
 * Operator endpoints for capturing and viewing meter readings.
 * Enforces business rules: active meters only, one reading per month/year, and increasing readings.
 */
@RestController
@RequestMapping("/api/meter-readings")
@PreAuthorize("hasRole('OPERATOR')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Meter Reading Capture", description = "Operator operations for capturing and listing meter readings")
public class MeterReadingController {

    private final MeterReadingService meterReadingService;

    public MeterReadingController(MeterReadingService meterReadingService) {
        this.meterReadingService = meterReadingService;
    }

    @Operation(summary = "Capture meter reading", description = "Records a new reading. Validates meter is ACTIVE, current > previous, and month/year uniqueness.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reading captured",
                     content = @Content(schema = @Schema(implementation = MeterReadingResponse.class))),
        @ApiResponse(responseCode = "400", description = "Business rule violation (inactive meter, duplicate period, or non-increasing reading)")
    })
    @PostMapping
    public ResponseEntity<MeterReadingResponse> create(@Valid @RequestBody MeterReadingRequest request) {
        return ResponseEntity.ok(meterReadingService.create(request));
    }

    @Operation(summary = "List all readings", description = "Paginated list of all meter readings with meter details")
    @ApiResponse(responseCode = "200", description = "List of readings returned")
    @GetMapping
    public ResponseEntity<Page<MeterReadingResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(meterReadingService.findAll(pageable));
    }

    @Operation(summary = "Get reading by ID", description = "Retrieves a single meter reading record")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reading found",
                     content = @Content(schema = @Schema(implementation = MeterReadingResponse.class))),
        @ApiResponse(responseCode = "404", description = "Reading not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MeterReadingResponse> getById(
            @Parameter(description = "Reading UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(meterReadingService.findById(id));
    }
}
