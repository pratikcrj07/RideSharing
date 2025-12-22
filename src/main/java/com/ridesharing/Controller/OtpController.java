package com.ridesharing.Controller;

import com.ridesharing.DTOs.*;
import com.ridesharing.Services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
public class OtpController {

    private final AuthService authService;

    private <T> ResponseEntity<Map<String, Object>> wrapResponse(T data, String message) {
        return ResponseEntity.ok(Map.of(
                "timestamp", Instant.now(),
                "status", 200,
                "message", message,
                "data", data
        ));
    }

    // ================= VERIFY OTP FOR REGISTRATION =================
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody OtpVerifyRequest req) {
        authService.verifyOtp(req);
        return wrapResponse(null, "Account registered successfully");
    }
}