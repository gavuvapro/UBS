package com.utilitybilling.controller;

import com.utilitybilling.dto.request.PaymentRequest;
import com.utilitybilling.dto.response.PaymentResponse;
import com.utilitybilling.service.PaymentService;
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

import java.util.List;
import java.util.UUID;

/**
 * Finance role endpoints for recording and querying payments.
 * Supports partial payments until a bill's outstanding balance reaches zero.
 */
@RestController
@RequestMapping("/api/payments")
@PreAuthorize("hasRole('FINANCE')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Payment Processing", description = "Finance operations for recording and viewing payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "Record payment", description = "Records a payment against a bill. Validates amount > 0 and not exceeding outstanding balance. Updates bill status to PAID when balance reaches zero. Triggers email confirmation and notification log on full payment.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment recorded successfully",
                     content = @Content(schema = @Schema(implementation = PaymentResponse.class))),
        @ApiResponse(responseCode = "400", description = "Business rule violation (already paid, overpayment, invalid amount)"),
        @ApiResponse(responseCode = "404", description = "Bill not found")
    })
    @PostMapping
    public ResponseEntity<PaymentResponse> recordPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.ok(paymentService.recordPayment(request));
    }

    @Operation(summary = "List all payments", description = "Paginated list of all recorded payments across all bills")
    @ApiResponse(responseCode = "200", description = "List of payments returned")
    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> listAll(Pageable pageable) {
        return ResponseEntity.ok(paymentService.findAll(pageable));
    }

    @Operation(summary = "Get payments by bill", description = "Retrieves all payments recorded for a specific bill ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payments returned"),
        @ApiResponse(responseCode = "404", description = "Bill not found")
    })
    @GetMapping("/bill/{billId}")
    public ResponseEntity<List<PaymentResponse>> getByBill(
            @Parameter(description = "Bill UUID", required = true) @PathVariable UUID billId) {
        return ResponseEntity.ok(paymentService.findByBillId(billId));
    }
}
