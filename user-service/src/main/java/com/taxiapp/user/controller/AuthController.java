package com.taxiapp.user.controller;

import com.taxiapp.dto.AuthRequest;
import com.taxiapp.dto.AuthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        String token = "token-" + request.getEmail() + "-" + System.currentTimeMillis();
        return ResponseEntity.ok(new AuthResponse(token, 1L, request.getEmail()));
    }
}