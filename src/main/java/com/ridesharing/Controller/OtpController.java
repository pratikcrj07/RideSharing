package com.ridesharing.Controller;

import com.ridesharing.DTOs.OtpRequest;
import com.ridesharing.DTOs.OtpVerifyRequest;
import com.ridesharing.DTOs.OtpResponse;
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

    @PostMapping("/send")
    public ResponseEntity<OtpResponse> sendOtp(@Valid @RequestBody OtpRequest request) {
        try {
            String otp = otpService.generateAndStoreOtp(request.getEmail());
            emailService.sendOtp(request.getEmail(), otp);
            return ResponseEntity.ok(new OtpResponse(true, "OTP sent to email successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(new OtpResponse(false, e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace(); // log
            return ResponseEntity.status(500).body(new OtpResponse(false, "Something went wrong"));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<OtpResponse> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        try {
            boolean valid = otpService.validateOtp(request.getEmail(), request.getOtp());
            if (valid) {
                return ResponseEntity.ok(new OtpResponse(true, "OTP verified successfully"));
            } else {
                return ResponseEntity.badRequest().body(new OtpResponse(false, "Invalid or expired OTP"));
            }
        } catch (Exception e) {
            e.printStackTrace(); // log
            return ResponseEntity.status(500)
                    .body(new OtpResponse(false, "Something went wrong"));
        }
    }
}
