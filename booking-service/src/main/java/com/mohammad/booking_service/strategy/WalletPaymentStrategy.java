package com.mohammad.booking_service.strategy;

import com.mohammad.booking_service.dto.PaymentRequest;
import com.mohammad.booking_service.entity.Booking;
import com.mohammad.booking_service.entity.Payment;
import com.mohammad.booking_service.entity.PaymentMethod;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class WalletPaymentStrategy implements PaymentStrategy {
    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.WALLET;
    }

    @Override
    public Payment processPayment(Booking booking, PaymentRequest paymentRequest) {
        return new Payment.Builder()
                .booking(booking)
                .paymentMethod(getPaymentMethod().name())
                .paymentTime(LocalDateTime.now())
                .userId(booking.getUserId())
                .build();
    }
}
