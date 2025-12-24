package com.ridesharing.Controller;

import com.ridesharing.DTOs.*;
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
@RequestMapping("/api/otp")
@RequiredArgsConstructor
public class OtpController {

    private final AuthService authService;

    private <T> ResponseEntity<Map<String, Object>> wrapResponse(T data, String message) {

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now());
        response.put("status", HttpStatus.OK.value());
        response.put("message", message);
        response.put("data", data);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody OtpVerifyRequest req) {
        authService.verifyOtp(req);
        return wrapResponse(null, "Account registered successfully");
    }
}