package com.mohammad.booking_service.controller;

import com.mohammad.booking_service.dto.PaymentRequest;
import com.mohammad.booking_service.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "APIs for processing booking payments")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @Operation(summary = "Process booking payment", description = "Process payment for a pending booking and confirm the booking upon success.")
    @PostMapping("/pay/{reference}")
    public ResponseEntity<String> payForBooking(@PathVariable String reference, @Valid @RequestBody PaymentRequest paymentRequest, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return paymentService.payForBooking(reference, paymentRequest, token);
    }
}
