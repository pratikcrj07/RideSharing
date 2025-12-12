package com.ridesharing.Controller;

import com.ridesharing.DTOs.*;
import com.ridesharing.Entities.Admin;
import com.ridesharing.Entities.Driver;
import com.ridesharing.Entities.User;
import com.ridesharing.Services.AuthService;
import com.ridesharing.Util.GoogleTokenVerifier;
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

//    @PostMapping("/driver/")
//    public ResponseEntity<Map<String, Object>> registerDriver(@Valid @RequestBody RegisterRequest req) {
//        Driver driver = authService.registerDriver(req);
//        return wrapResponse(driver, "Driver registered successfully");
//    }

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
    public ResponseEntity<?> googleLogin(@RequestBody @Valid GoogleAuthRequest request) {
        try {
            GoogleTokenVerifier.Payload payload = GoogleTokenVerifier.verify(request.getIdToken());

            if (!payload.isEmailVerified()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("status", 401, "message", "Email not verified by Google"));
            }

            // Check if user exists or create new user
            User user = UserService.findOrCreateUser(payload.getEmail(), payload.getName());

            // Generate JWT tokens
            String accessToken = jwtService.generateAccessToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            Map<String, Object> response = Map.of(
                    "accessToken", accessToken,
                    "refreshToken", refreshToken,
                    "tokenType", "Bearer"
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("status", 401, "message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", 500, "message", "Something went wrong"));
        }
    }



    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "timestamp", Instant.now()
        ));
    }
}
