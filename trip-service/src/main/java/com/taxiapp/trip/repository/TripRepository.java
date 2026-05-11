package com.taxiapp.trip.repository;

import com.taxiapp.trip.entity.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDateTime;
import java.util.List;

public interface TripRepository extends JpaRepository<Trip, Long> {
    
    List<Trip> findByPassengerId(Long passengerId);
    
    List<Trip> findByDriverId(Long driverId);
    
    @Query("SELECT COUNT(t) FROM Trip t WHERE t.createdAt BETWEEN :start AND :end")
    Long countTripsByDateRange(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT AVG(t.price) FROM Trip t WHERE t.createdAt BETWEEN :start AND :end")
    Double averagePriceByDateRange(LocalDateTime start, LocalDateTime end);
}