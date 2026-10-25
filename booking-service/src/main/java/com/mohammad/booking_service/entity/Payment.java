package com.mohammad.booking_service.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentMethod;

    private LocalDateTime paymentTime;

    @OneToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;

    private Integer userId;

    public Payment(){

    }

    public Payment(String paymentMethod, LocalDateTime paymentTime, Booking booking, Integer userId){
        this.paymentMethod = paymentMethod;
        this.paymentTime = paymentTime;
        this.booking = booking;
        this.userId = userId;
    }

    public String getPaymentMethod(){
        return paymentMethod;
    }

    public LocalDateTime getPaymentTime(){
        return paymentTime;
    }

    public Booking getBooking(){
        return booking;
    }

    public Integer getUserId(){
        return userId;
    }

    public void setPaymentMethod(String paymentMethod){
        this.paymentMethod = paymentMethod;
    }

    public void setPaymentTime(LocalDateTime paymentTime){
        this.paymentTime = paymentTime;
    }

    public void setBooking(Booking booking){
        this.booking = booking;
    }

    public void setUserId(Integer userId){
        this.userId = userId;
    }

    public static class Builder {
        public String paymentMethod;
        public LocalDateTime paymentTime;
        public Booking booking;
        public Integer userId;

        public Builder paymentMethod(String paymentMethod){
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Builder paymentTime(LocalDateTime paymentTime){
            this.paymentTime = paymentTime;
            return this;
        }

        public Builder booking(Booking booking){
            this.booking = booking;
            return this;
        }

        public Builder userId(Integer userId){
            this.userId = userId;
            return this;
        }

        public Payment build(){
            return new Payment(paymentMethod, paymentTime, booking, userId);
        }
    }
}
