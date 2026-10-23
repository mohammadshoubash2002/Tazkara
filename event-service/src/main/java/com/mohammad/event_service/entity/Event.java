package com.mohammad.event_service.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "events")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    private String description;
    private String venue;
    private String city;
    private LocalDateTime dateTime;
    private String status;

    @Size(min = 1, max = 5, message = "An event must have between 1 and 5 ticket types")
    @OneToMany(mappedBy = "event")
    private List<TicketType> ticketType;

    public Event() {
    }

    public Event(String title, String description, String venue, String city, LocalDateTime dateTime, String status) {
        this.title = title;
        this.description = description;
        this.venue = venue;
        this.city = city;
        this.dateTime = dateTime;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Event{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", venue='" + venue + '\'' +
                ", city='" + city + '\'' +
                ", dateTime=" + dateTime +
                ", status='" + status + '\'' +
                '}';
    }

    public static class Builder {
        private String title;
        private String description;
        private String venue;
        private String city;
        private LocalDateTime dateTime;
        private String status;

        public Builder title(String title){
            this.title = title;
            return this;
        }

        public Builder description(String description){
            this.description = description;
            return this;
        }

        public Builder venue(String venue){
            this.venue = venue;
            return this;
        }

        public Builder city(String city){
            this.city = city;
            return this;
        }

        public Builder dateTime(LocalDateTime dateTime){
            this.dateTime = dateTime;
            return this;
        }

        public Builder status(String status){
            this.status = status;
            return this;
        }

        public Event build() {
            return new Event(title, description, venue, city, dateTime, status);
        }
    }
}
