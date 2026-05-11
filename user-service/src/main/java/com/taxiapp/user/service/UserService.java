package com.taxiapp.user.service;

import com.taxiapp.dto.DriverResponse;
import com.taxiapp.dto.PassengerResponse;
import com.taxiapp.user.entity.Driver;
import com.taxiapp.user.entity.Passenger;
import com.taxiapp.user.repository.DriverRepository;
import com.taxiapp.user.repository.PassengerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final PassengerRepository passengerRepository;
    private final DriverRepository driverRepository;

    public UserService(PassengerRepository passengerRepository, DriverRepository driverRepository) {
        this.passengerRepository = passengerRepository;
        this.driverRepository = driverRepository;
    }

    public PassengerResponse registerPassenger(String name, String email, String phone) {
        Passenger p = new Passenger();
        p.setName(name);
        p.setEmail(email);
        p.setPhone(phone);
        p = passengerRepository.save(p);
        log.info("Passenger registered: id={}, name={}", p.getId(), p.getName());
        return toPassengerResponse(p);
    }

    public PassengerResponse getPassenger(Long id) {
        Passenger p = passengerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Passenger not found: " + id));
        return toPassengerResponse(p);
    }

    public DriverResponse registerDriver(String name, String email, String phone, String license) {
        Driver d = new Driver();
        d.setName(name);
        d.setEmail(email);
        d.setPhone(phone);
        d.setLicenseNumber(license);
        d.setStatus(Driver.DriverStatus.AVAILABLE);
        d = driverRepository.save(d);
        log.info("Driver registered: id={}, name={}", d.getId(), d.getName());
        return toDriverResponse(d);
    }

    @Cacheable(value = "drivers", key = "#id")
    public DriverResponse getDriver(Long id) {
        log.info("Fetching driver {} from database", id);
        Driver d = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + id));
        return toDriverResponse(d);
    }

    @Transactional
    @CacheEvict(value = "drivers", key = "#id")
    public DriverResponse updateStatus(Long id, String newStatus) {
        Driver d = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Driver not found: " + id));
        d.setStatus(Driver.DriverStatus.valueOf(newStatus.toUpperCase()));
        d = driverRepository.save(d);
        log.info("Driver {} status updated to {}", id, newStatus);
        return toDriverResponse(d);
    }

    @Transactional
    public Optional<DriverResponse> findAvailableDriver() {
        return driverRepository.findAvailableDriverWithLock().map(d -> {
            d.setStatus(Driver.DriverStatus.BUSY);
            driverRepository.save(d);
            log.info("Driver {} assigned, status -> BUSY", d.getId());
            return toDriverResponse(d);
        });
    }

    private PassengerResponse toPassengerResponse(Passenger p) {
        PassengerResponse r = new PassengerResponse();
        r.setId(p.getId());
        r.setName(p.getName());
        r.setEmail(p.getEmail());
        r.setPhone(p.getPhone());
        return r;
    }

    private DriverResponse toDriverResponse(Driver d) {
        DriverResponse r = new DriverResponse();
        r.setId(d.getId());
        r.setName(d.getName());
        r.setEmail(d.getEmail());
        r.setPhone(d.getPhone());
        r.setLicenseNumber(d.getLicenseNumber());
        r.setStatus(d.getStatus().name());
        return r;
    }
}