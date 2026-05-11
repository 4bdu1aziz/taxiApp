package com.taxiapp.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TripResponse {
    private Long id;
    private Long passengerId;
    private Long driverId;
    private String status;
    private String origin;
    private String destination;
    private BigDecimal price;
    private Integer rating;
    private LocalDateTime createdAt;

    public TripResponse() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPassengerId() { return passengerId; }
    public void setPassengerId(Long passengerId) { this.passengerId = passengerId; }
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}