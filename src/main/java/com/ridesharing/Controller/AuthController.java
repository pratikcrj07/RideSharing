package com.ridesharing.Controller;

import com.ridesharing.DTOs.*;
import com.ridesharing.Entities.Admin;
import com.ridesharing.Entities.Driver;
import com.ridesharing.Entities.User;
import com.ridesharing.Services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/user")
    public ResponseEntity<User> registerUser(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.registerUser(req));
    }

    @PostMapping("/register/driver")
    public ResponseEntity<Driver> registerDriver(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.registerDriver(req));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<Admin> registerAdmin(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.registerAdmin(req));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    // OTP: request OTP (returns OTP in response for dev; integrate email/SMS in prod)
    @PostMapping("/otp/send")
    public ResponseEntity<String> sendOtp(@Valid @RequestBody OtpRequest req) {
        String otp = authService.sendOtp(req);
        // Dev behavior: return OTP for easier testing
        return ResponseEntity.ok(otp);
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest req) {
        return ResponseEntity.ok(authService.verifyOtp(req));
    }

    // Google id_token login (frontend should acquire id_token from Google Sign-In and post it here)
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> google(@Valid @RequestBody GoogleAuthRequest req) {
        return ResponseEntity.ok(authService.googleLogin(req.getIdToken()));
    }
}
