package com.mohammad.booking_service.strategy;

import com.mohammad.booking_service.dto.PaymentRequest;
import com.mohammad.booking_service.entity.Booking;
import com.mohammad.booking_service.entity.Payment;
import com.mohammad.booking_service.entity.PaymentMethod;

public interface PaymentStrategy {
    PaymentMethod getPaymentMethod();
    Payment processPayment(Booking booking, PaymentRequest paymentRequest);
}
