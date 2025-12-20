package com.ridesharing.Controller;

import com.ridesharing.DTOs.*;
import com.ridesharing.Services.AuthService;
import com.ridesharing.Services.EmailService;
import com.ridesharing.Services.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
@RequiredArgsConstructor
public class OtpController {

    private final OtpService otpService;
    private final EmailService emailService;
    private final AuthService authService;

    @PostMapping("/send")
    public ResponseEntity<OtpResponse> sendOtp(@Valid @RequestBody OtpRequest req) {
        String otp = otpService.generateAndStoreOtp(req.getEmail());
        emailService.sendOtp(req.getEmail(), otp);
        return ResponseEntity.ok(new OtpResponse(true, "OTP sent successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> loginWithOtp(@Valid @RequestBody OtpVerifyRequest req) {
        return ResponseEntity.ok(authService.loginWithOtp(req));
    }
}
