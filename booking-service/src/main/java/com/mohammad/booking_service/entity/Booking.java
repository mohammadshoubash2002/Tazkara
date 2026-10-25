package com.mohammad.booking_service.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer userId;

    private Integer eventId;

    private Integer ticketsCount;

    public String status;

    public Booking(){

    }

    public Booking(Integer userId, Integer eventId, Integer ticketsCount, String status) {
        this.userId = userId;
        this.eventId = eventId;
        this.ticketsCount = ticketsCount;
        this.status = status;
    }

    public Integer getEventId() {
        return eventId;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getTicketsCount() {
        return ticketsCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public void setTicketsCount(Integer ticketsCount) {
        this.ticketsCount = ticketsCount;
    }

    public static class Builder{
        private Integer userId;
        private Integer eventId;
        private Integer ticketsCount;
        public String status;

        public Builder userId(Integer userId){
            this.userId = userId;
            return this;
        }

        public Builder eventId(Integer eventId){
            this.eventId = eventId;
            return this;
        }

        public Builder ticketsCount(Integer ticketsCount){
            this.ticketsCount = ticketsCount;
            return this;
        }

        public Builder status(String status){
            this.status = status;
            return this;
        }

        public Booking build(){
            return new Booking(userId, eventId, ticketsCount, status);
        }
    }
}
