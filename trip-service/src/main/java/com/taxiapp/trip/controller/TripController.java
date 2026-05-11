package com.taxiapp.trip.controller;

import com.taxiapp.trip.entity.Trip;
import com.taxiapp.trip.service.TripService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping("/trips")
    public ResponseEntity<Trip> createTrip(@RequestBody Trip trip) {
        return ResponseEntity.ok(tripService.createTrip(trip));
    }

    @GetMapping("/trips/{id}")
    public ResponseEntity<Trip> getTrip(@PathVariable Long id) {
        return ResponseEntity.ok(tripService.getTrip(id));
    }

    @GetMapping("/trips")
    public ResponseEntity<List<Trip>> getPassengerTrips(@RequestParam("passenger_id") Long passengerId) {
        return ResponseEntity.ok(tripService.getPassengerTrips(passengerId));
    }

    @PatchMapping("/trips/{id}/status")
    public ResponseEntity<Trip> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(tripService.updateStatus(id, body.get("status")));
    }

    @PostMapping("/trips/{id}/rate")
    public ResponseEntity<Trip> rateTrip(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        return ResponseEntity.ok(tripService.rateTrip(id, body.get("rating")));
    }

    @GetMapping("/stats/daily")
    public ResponseEntity<Map<String, Object>> getDailyStats() {
        return ResponseEntity.ok(tripService.getDailyStats());
    }
}