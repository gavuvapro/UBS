package com.utilitybilling.controller;

import com.utilitybilling.dto.request.MeterRequest;
import com.utilitybilling.dto.response.MeterResponse;
import com.utilitybilling.service.MeterService;
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
 * Admin-only endpoints for meter management.
 * Meters are linked to customers and can be deactivated (soft-delete).
 */
@RestController
@RequestMapping("/api/meters")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Meter Management", description = "Admin operations for managing utility meters")
public class MeterController {

    private final MeterService meterService;

    public MeterController(MeterService meterService) {
        this.meterService = meterService;
    }

    @Operation(summary = "Create a new meter", description = "Registers a unique meter number linked to a customer. Validates uniqueness globally.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Meter created",
                     content = @Content(schema = @Schema(implementation = MeterResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error or duplicate meter number")
    })
    @PostMapping
    public ResponseEntity<MeterResponse> create(@Valid @RequestBody MeterRequest request) {
        return ResponseEntity.ok(meterService.create(request));
    }

    @Operation(summary = "List all meters", description = "Paginated list of all meters with customer details")
    @ApiResponse(responseCode = "200", description = "List of meters returned")
    @GetMapping
    public ResponseEntity<Page<MeterResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(meterService.findAll(pageable));
    }

    @Operation(summary = "Get meter by ID", description = "Retrieves meter details including linked customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Meter found",
                     content = @Content(schema = @Schema(implementation = MeterResponse.class))),
        @ApiResponse(responseCode = "404", description = "Meter not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<MeterResponse> getById(
            @Parameter(description = "Meter UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(meterService.findById(id));
    }

    @Operation(summary = "Update meter", description = "Updates meter type, number, installation date, or linked customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Meter updated",
                     content = @Content(schema = @Schema(implementation = MeterResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "Meter or customer not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<MeterResponse> update(
            @Parameter(description = "Meter UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody MeterRequest request) {
        return ResponseEntity.ok(meterService.update(id, request));
    }

    @Operation(summary = "Deactivate meter", description = "Sets meter status to INACTIVE. Inactive meters cannot receive readings.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Meter deactivated"),
        @ApiResponse(responseCode = "404", description = "Meter not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Meter UUID", required = true) @PathVariable UUID id) {
        meterService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
