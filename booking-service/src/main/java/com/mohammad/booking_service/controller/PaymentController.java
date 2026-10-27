package com.mohammad.booking_service.controller;

import com.mohammad.booking_service.dto.PaymentRequest;
import com.mohammad.booking_service.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;

    @PostMapping("/pay/{booking_id}")
    public ResponseEntity<String> payForBooking(@PathVariable Integer booking_id, @Valid @RequestBody PaymentRequest paymentRequest, HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return paymentService.payForBooking(booking_id, paymentRequest, token);
    }
}
