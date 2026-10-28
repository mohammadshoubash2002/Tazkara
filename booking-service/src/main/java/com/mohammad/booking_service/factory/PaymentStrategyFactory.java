package com.mohammad.booking_service.factory;

import com.mohammad.booking_service.entity.PaymentMethod;
import com.mohammad.booking_service.exception.PaymentMethodException;
import com.mohammad.booking_service.strategy.PaymentStrategy;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.List;

@Component
public class PaymentStrategyFactory {
    private final Map<PaymentMethod, PaymentStrategy> strategies = new EnumMap<>(PaymentMethod.class);

    public PaymentStrategyFactory(List<PaymentStrategy> strategyList) {
        for (PaymentStrategy strategy : strategyList) {
            strategies.put(strategy.getPaymentMethod(), strategy);
        }
    }

    public PaymentStrategy getStrategy(String paymentMethodStr) {
        try {
            PaymentMethod method = PaymentMethod.valueOf(paymentMethodStr.toUpperCase());
            PaymentStrategy strategy = strategies.get(method);
            if (strategy == null) {
                throw new PaymentMethodException("Payment method not supported: " + paymentMethodStr);
            }
            return strategy;
        } catch (IllegalArgumentException ex) {
            throw new PaymentMethodException("Invalid payment method: " + paymentMethodStr);
        }
    }
}
