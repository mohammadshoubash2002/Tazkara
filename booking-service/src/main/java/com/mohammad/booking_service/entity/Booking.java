package com.mohammad.booking_service.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bookings")
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String reference;

    private Integer userId;

    private Integer eventId;

    private Integer ticketsCount;

    private Double totalAmount;

    @Column(nullable = false)
    private String status;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;

    @Column(nullable = true)
    private String cancellationReason;

    @Column(nullable = true)
    private Double refund;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookingItem> items = new ArrayList<>();

    public Booking() {
    }

    public Booking(String reference, Integer userId, Integer eventId, Integer ticketsCount, Double totalAmount, String status, LocalDateTime expiresAt, LocalDateTime createdAt, String cancellationReason, Double refund, List<BookingItem> items) {
        this.reference = reference;
        this.userId = userId;
        this.eventId = eventId;
        this.ticketsCount = ticketsCount;
        this.totalAmount = totalAmount;
        this.status = status;
        this.expiresAt = expiresAt;
        this.createdAt = createdAt;
        this.cancellationReason = cancellationReason;
        this.refund = refund;
        if (items != null) {
            this.items = items;
            for (BookingItem item : items) {
                item.setBooking(this);
            }
        }
    }

    public void addItem(BookingItem item) {
        items.add(item);
        item.setBooking(this);
    }

    public Long getId() {
        return id;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public Integer getTicketsCount() {
        return ticketsCount;
    }

    public void setTicketsCount(Integer ticketsCount) {
        this.ticketsCount = ticketsCount;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCancellationReason() {
        return cancellationReason;
    }

    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }

    public Double getRefund() {
        return refund;
    }

    public void setRefund(Double refund) {
        this.refund = refund;
    }

    public List<BookingItem> getItems() {
        return items;
    }

    public void setItems(List<BookingItem> items) {
        this.items = items;
    }

    public static class Builder {
        private String reference;
        private Integer userId;
        private Integer eventId;
        private Integer ticketsCount;
        private Double totalAmount;
        private String status;
        private LocalDateTime expiresAt;
        private LocalDateTime createdAt;
        private String cancellationReason;
        private Double refund;
        private List<BookingItem> items = new ArrayList<>();

        public Builder reference(String reference) {
            this.reference = reference;
            return this;
        }

        public Builder userId(Integer userId) {
            this.userId = userId;
            return this;
        }

        public Builder eventId(Integer eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder ticketsCount(Integer ticketsCount) {
            this.ticketsCount = ticketsCount;
            return this;
        }

        public Builder totalAmount(Double totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder cancellationReason(String cancellationReason) {
            this.cancellationReason = cancellationReason;
            return this;
        }

        public Builder refund(Double refund) {
            this.refund = refund;
            return this;
        }

        public Builder items(List<BookingItem> items) {
            this.items = items;
            return this;
        }

        public Booking build() {
            return new Booking(reference, userId, eventId, ticketsCount, totalAmount, status, expiresAt, createdAt, cancellationReason, refund, items);
        }
    }
}
