package com.utilitybilling.controller;

import com.utilitybilling.dto.request.CustomerRequest;
import com.utilitybilling.dto.response.CustomerResponse;
import com.utilitybilling.service.CustomerService;
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
 * Admin-only endpoints for customer lifecycle management.
 * Supports creation, listing, update, and soft-delete (status INACTIVE).
 */
@RestController
@RequestMapping("/api/customers")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Customer Management", description = "Admin operations for managing utility customers")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Operation(summary = "Create a new customer", description = "Registers a customer with unique national ID. Returns 400 if national ID already exists.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer created successfully",
                     content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error or duplicate national ID")
    })
    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.create(request));
    }

    @Operation(summary = "List all customers", description = "Paginated list of all registered customers")
    @ApiResponse(responseCode = "200", description = "List of customers returned")
    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> list(Pageable pageable) {
        return ResponseEntity.ok(customerService.findAll(pageable));
    }

    @Operation(summary = "Get customer by ID", description = "Retrieves a single customer record by UUID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer found",
                     content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(
            @Parameter(description = "Customer UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(customerService.findById(id));
    }

    @Operation(summary = "Update customer", description = "Updates customer details including status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Customer updated",
                     content = @Content(schema = @Schema(implementation = CustomerResponse.class))),
        @ApiResponse(responseCode = "400", description = "Validation error or duplicate national ID"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(
            @Parameter(description = "Customer UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.update(id, request));
    }

    @Operation(summary = "Soft-delete customer", description = "Sets customer status to INACTIVE. Inactive customers cannot receive new bills.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Customer deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Customer UUID", required = true) @PathVariable UUID id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
