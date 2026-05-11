package com.taxiapp.trip.service;

import com.taxiapp.dto.DriverResponse;
import com.taxiapp.dto.PassengerResponse;
import com.taxiapp.trip.entity.Trip;
import com.taxiapp.trip.repository.TripRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class TripService {

    private static final Logger log = LoggerFactory.getLogger(TripService.class);
    private final TripRepository tripRepository;
    private final RestTemplate restTemplate;
    private final Random random = new Random();

    private final String userServiceUrl = "http://user-service:8081/api";
    private final String notificationServiceUrl = "http://notification-service:8083/api";

    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
        this.restTemplate = new RestTemplate();
    }

    @Transactional
    public Trip createTrip(Trip trip) {
        try {
            restTemplate.getForObject(userServiceUrl + "/passengers/" + trip.getPassengerId(), PassengerResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Passenger not found: " + trip.getPassengerId());
        }

        trip.setPrice(calculatePrice());
        trip.setStatus(Trip.TripStatus.CREATED);

        try {
            DriverResponse driver = restTemplate.getForObject(userServiceUrl + "/drivers/available", DriverResponse.class);
            if (driver != null) {
                trip.setDriverId(driver.getId());
                trip.setStatus(Trip.TripStatus.ACCEPTED);
                log.info("Driver {} assigned to trip {}", driver.getId(), trip.getId());
            }
        } catch (Exception e) {
            log.warn("No available drivers");
        }

        trip = tripRepository.save(trip);
        log.info("Trip {} created with status {}", trip.getId(), trip.getStatus());

        sendNotifications(trip, "created");
        return trip;
    }

    public Trip getTrip(Long id) {
        return tripRepository.findById(id).orElseThrow(() -> new RuntimeException("Trip not found: " + id));
    }

    public List<Trip> getPassengerTrips(Long passengerId) {
        return tripRepository.findByPassengerId(passengerId);
    }

    @Transactional
    public Trip updateStatus(Long id, String newStatus) {
        Trip trip = getTrip(id);
        trip.setStatus(Trip.TripStatus.valueOf(newStatus.toUpperCase()));
        trip = tripRepository.save(trip);
        log.info("Trip {} status -> {}", id, newStatus);

        if (trip.getDriverId() != null) {
            try {
                String driverStatus = null;
                if ("ACCEPTED".equalsIgnoreCase(newStatus) || "STARTED".equalsIgnoreCase(newStatus)) {
                    driverStatus = "BUSY";
                } else if ("COMPLETED".equalsIgnoreCase(newStatus) || "CANCELLED".equalsIgnoreCase(newStatus)) {
                    driverStatus = "AVAILABLE";
                }
                if (driverStatus != null) {
                    Map<String, String> body = new HashMap<>();
                    body.put("status", driverStatus);
                    restTemplate.patchForObject(userServiceUrl + "/drivers/" + trip.getDriverId() + "/status", body, Object.class);
                    log.info("Driver {} status -> {}", trip.getDriverId(), driverStatus);
                }
            } catch (Exception e) {
                log.error("Failed to update driver status: {}", e.getMessage());
            }
        }

        sendNotifications(trip, "status changed to " + newStatus);
        return trip;
    }

    @Transactional
    public Trip rateTrip(Long id, Integer rating) {
        if (rating < 1 || rating > 5) throw new RuntimeException("Rating must be 1-5");
        Trip trip = getTrip(id);
        trip.setRating(rating);
        return tripRepository.save(trip);
    }

    public Map<String, Object> getDailyStats() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTrips", tripRepository.countTripsByDateRange(start, end));
        stats.put("averagePrice", tripRepository.averagePriceByDateRange(start, end));
        return stats;
    }

    private BigDecimal calculatePrice() {
        double distance = 5 + random.nextDouble() * 20;
        return BigDecimal.valueOf(distance * 50.0).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private void sendNotifications(Trip trip, String event) {
        try {
            Map<String, Object> notif = new HashMap<>();
            notif.put("tripId", trip.getId());
            notif.put("recipientType", "PASSENGER");
            notif.put("recipientId", trip.getPassengerId());
            notif.put("message", "Trip " + trip.getId() + " " + event);
            restTemplate.postForObject(notificationServiceUrl + "/notifications", notif, Object.class);

            if (trip.getDriverId() != null) {
                notif.put("recipientType", "DRIVER");
                notif.put("recipientId", trip.getDriverId());
                notif.put("message", "Trip " + trip.getId() + " " + event);
                restTemplate.postForObject(notificationServiceUrl + "/notifications", notif, Object.class);
            }
        } catch (Exception e) {
            log.error("Notification failed: {}", e.getMessage());
        }
    }
}