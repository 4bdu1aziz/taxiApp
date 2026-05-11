package com.taxiapp.trip.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Index;

@Entity
@Table(name = "trips", indexes = {
        @Index(name = "idx_trip_status", columnList = "status")
})
public class Trip {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "passenger_id", nullable = false)
    private Long passengerId;
    
    @Column(name = "driver_id")
    private Long driverId;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TripStatus status;
    
    @Column(nullable = false)
    private String origin;
    
    @Column(nullable = false)
    private String destination;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    
    private Integer rating;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = TripStatus.CREATED;
    }
    
    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum TripStatus {
        CREATED, ACCEPTED, STARTED, COMPLETED, CANCELLED
    }
    
    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPassengerId() { return passengerId; }
    public void setPassengerId(Long passengerId) { this.passengerId = passengerId; }
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    public TripStatus getStatus() { return status; }
    public void setStatus(TripStatus status) { this.status = status; }
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
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}