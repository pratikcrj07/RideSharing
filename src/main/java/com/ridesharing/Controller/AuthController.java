package com.ridesharing.Controller;

import com.ridesharing.DTOs.*;
import com.ridesharing.Entities.Admin;

import com.ridesharing.Services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;



    private <T> ResponseEntity<Map<String, Object>> wrapResponse(T data, String message) {

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now());
        response.put("status", HttpStatus.OK.value());
        response.put("message", message);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register/user")
    public ResponseEntity<Map<String, Object>> registerUser(@Valid @RequestBody RegisterRequest req) {
        authService.registerUser(req);
        return wrapResponse(null, "\"User registered. OTP sent for verification.\"\n");
    }



    @PostMapping("/register/admin")
    public ResponseEntity<Map<String, Object>> registerAdmin(@Valid @RequestBody RegisterRequest req) {
        Admin admin = authService.registerAdmin(req);
        return wrapResponse(admin, "Admin registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        AuthResponse auth = authService.login(req);
        return wrapResponse(auth, "Login successful");
    }




    @PostMapping("/google")
    public ResponseEntity<Map<String, Object>> googleLogin(@Valid @RequestBody GoogleAuthRequest req) {
        AuthResponse auth = authService.googleLogin(req.getIdToken());
        return wrapResponse(auth, "Google login successful");
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", Instant.now()
        ));
    }
}
