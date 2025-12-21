package com.ridesharing.Controller;

import com.ridesharing.DTOs.*;
import com.ridesharing.Entities.Admin;
import com.ridesharing.Entities.Driver;
import com.ridesharing.Entities.User;
import com.ridesharing.Services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    private <T> ResponseEntity<Map<String, Object>> wrapResponse(T data, String message) {
        return ResponseEntity.ok(Map.of(
                "timestamp", Instant.now(),
                "status", HttpStatus.OK.value(),
                "message", message,
                "data", data
        ));
    }

    @PostMapping("/register/user")
    public ResponseEntity<Map<String, Object>> registerUser(@Valid @RequestBody RegisterRequest req) {
        User user = authService.registerUser(req);
        return wrapResponse(user, "User registered successfully");
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
