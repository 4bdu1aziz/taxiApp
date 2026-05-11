package com.taxiapp.user.controller;

import com.taxiapp.dto.DriverResponse;
import com.taxiapp.dto.PassengerResponse;
import com.taxiapp.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/passengers")
    public ResponseEntity<PassengerResponse> registerPassenger(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                userService.registerPassenger(
                        body.get("name"),
                        body.get("email"),
                        body.get("phone")
                )
        );
    }

    @GetMapping("/passengers/{id}")
    public ResponseEntity<PassengerResponse> getPassenger(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getPassenger(id));
    }

    @PostMapping("/drivers")
    public ResponseEntity<DriverResponse> registerDriver(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                userService.registerDriver(
                        body.get("name"),
                        body.get("email"),
                        body.get("phone"),
                        body.get("licenseNumber")
                )
        );
    }

    @GetMapping("/drivers/{id}")
    public ResponseEntity<DriverResponse> getDriver(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getDriver(id));
    }

    @PatchMapping("/drivers/{id}/status")
    public ResponseEntity<DriverResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(userService.updateStatus(id, body.get("status")));
    }

    @GetMapping("/drivers/available")
    public ResponseEntity<DriverResponse> findAvailableDriver() {
        Optional<DriverResponse> driver = userService.findAvailableDriver();
        return driver.map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }
}