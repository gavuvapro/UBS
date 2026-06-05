package com.utilitybilling.controller;

import com.utilitybilling.dto.response.BillResponse;
import com.utilitybilling.entity.Customer;
import com.utilitybilling.repository.CustomerRepository;
import com.utilitybilling.security.UserDetailsImpl;
import com.utilitybilling.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Billing endpoints for generating, listing, and approving bills.
 * Role-based access controls restrict operations by ADMIN, FINANCE, OPERATOR, and CUSTOMER roles.
 * CUSTOMER role can only access their own bill records.
 */
@RestController
@RequestMapping("/api/bills")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Billing Engine", description = "Bill generation, approval, and customer queries")
public class BillingController {

    private final BillingService billingService;
    private final CustomerRepository customerRepository;

    public BillingController(BillingService billingService, CustomerRepository customerRepository) {
        this.billingService = billingService;
        this.customerRepository = customerRepository;
    }

    @Operation(summary = "Generate bill from meter reading", description = "Creates a PENDING bill for a meter reading. Validates customer is ACTIVE and applies current tariff/service charge/tax rules. Triggers email notification and DB notification log.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bill generated successfully",
                     content = @Content(schema = @Schema(implementation = BillResponse.class))),
        @ApiResponse(responseCode = "400", description = "Business rule violation (inactive customer, duplicate bill, missing tariff)"),
        @ApiResponse(responseCode = "404", description = "Meter reading not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN','OPERATOR')")
    @PostMapping("/generate/{meterReadingId}")
    public ResponseEntity<BillResponse> generateBill(
            @Parameter(description = "Meter Reading UUID", required = true) @PathVariable UUID meterReadingId) {
        return ResponseEntity.ok(billingService.generateBill(meterReadingId));
    }

    @Operation(summary = "List all bills", description = "Paginated list of all bills. Accessible to ADMIN and FINANCE roles only.")
    @ApiResponse(responseCode = "200", description = "List of bills returned")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @GetMapping
    public ResponseEntity<Page<BillResponse>> listAll(Pageable pageable) {
        return ResponseEntity.ok(billingService.findAll(pageable));
    }

    @Operation(summary = "Get bill by ID", description = "Retrieves a single bill. ADMIN/FINANCE can view any bill; CUSTOMER can only view their own bills.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bill found",
                     content = @Content(schema = @Schema(implementation = BillResponse.class))),
        @ApiResponse(responseCode = "403", description = "Customer attempting to access another customer's bill"),
        @ApiResponse(responseCode = "404", description = "Bill not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    @GetMapping("/{id}")
    public ResponseEntity<BillResponse> getById(
            @Parameter(description = "Bill UUID", required = true) @PathVariable UUID id) {
        BillResponse bill = billingService.findById(id);
        if (isCustomerOnly()) {
            Customer customer = getCurrentCustomer();
            if (!customer.getId().equals(bill.customerId())) {
                throw new org.springframework.security.access.AccessDeniedException("You can only view your own bills");
            }
        }
        return ResponseEntity.ok(bill);
    }

    @Operation(summary = "Approve bill", description = "Changes bill status from PENDING to APPROVED. Only ADMIN or FINANCE can approve bills.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bill approved",
                     content = @Content(schema = @Schema(implementation = BillResponse.class))),
        @ApiResponse(responseCode = "400", description = "Bill is not in PENDING status"),
        @ApiResponse(responseCode = "404", description = "Bill not found")
    })
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    @PutMapping("/{id}/approve")
    public ResponseEntity<BillResponse> approveBill(
            @Parameter(description = "Bill UUID", required = true) @PathVariable UUID id) {
        return ResponseEntity.ok(billingService.approveBill(id));
    }

    @Operation(summary = "List bills by customer", description = "Paginated list of bills for a specific customer. ADMIN/FINANCE can query any customer; CUSTOMER can only query their own customer ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of bills returned"),
        @ApiResponse(responseCode = "403", description = "Customer attempting to query another customer's bills")
    })
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE','CUSTOMER')")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Page<BillResponse>> listByCustomer(
            @Parameter(description = "Customer UUID", required = true) @PathVariable UUID customerId,
            Pageable pageable) {
        if (isCustomerOnly()) {
            Customer customer = getCurrentCustomer();
            if (!customer.getId().equals(customerId)) {
                throw new org.springframework.security.access.AccessDeniedException("You can only view your own bills");
            }
        }
        return ResponseEntity.ok(billingService.findByCustomerId(customerId, pageable));
    }

    /**
     * Checks if the current authenticated user has only the ROLE_CUSTOMER authority.
     */
    private boolean isCustomerOnly() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .allMatch(a -> a.getAuthority().equals("ROLE_CUSTOMER"));
    }

    /**
     * Retrieves the Customer entity linked to the currently authenticated user.
     * Throws an exception if the user is not linked to a customer profile.
     */
    private Customer getCurrentCustomer() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof UserDetailsImpl)) {
            throw new org.springframework.security.access.AccessDeniedException("Unable to identify current user");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        return customerRepository.findByUserId(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Customer profile not linked to user"));
    }
}
