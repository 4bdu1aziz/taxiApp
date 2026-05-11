package com.taxiapp.dto;

public class TripRequest {
    private Long passengerId;
    private String origin;
    private String destination;

    public TripRequest() {}

    public Long getPassengerId() { return passengerId; }
    public void setPassengerId(Long passengerId) { this.passengerId = passengerId; }
    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }
}