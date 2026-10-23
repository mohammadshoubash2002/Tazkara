package com.mohammad.event_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "ticket_types")
public class TicketType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(unique = true)
    private String name;

    private Double price;

    @Min(0)
    @Max(10000)
    private Integer quantity;

    @ManyToOne
    @JoinColumn(name = "event_id")
    @JsonIgnore
    private Event event;

    public TicketType(){

    }

    public TicketType(String name, Double price, Integer quantity, Event event){
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.event = event;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    @Override
    public String toString() {
        return "Event{" +
                "name='" + name + '\'' +
                ", price='" + price + '\'' +
                ", quantity='" + quantity + '\'' +
                ", event='" + event + '\'' +
                '}';
    }

    public static class Builder {
        private String name;
        private Double price;
        private Integer quantity;
        private Event event;

        public Builder name(String name){
            this.name = name;
            return this;
        }

        public Builder price(Double price){
            this.price = price;
            return this;
        }

        public Builder quantity(Integer quantity){
            this.quantity = quantity;
            return this;
        }

        public Builder event(Event event){
            this.event = event;
            return this;
        }

        public TicketType build(){
            return new TicketType(name, price, quantity, event);
        }
    }
}
