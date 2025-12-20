package com.ridesharing.Controller;

import com.ridesharing.Security.JwtUtil;
import com.ridesharing.Services.DriverApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/driver")
@RequiredArgsConstructor
public class AdminDriverApplication {

    private final DriverApplicationService driverService;
    private final JwtUtil jwtUtil;

    // -------- APPROVE DRIVER --------
    @PutMapping("/approve/{id}")
    public ResponseEntity<String> approve(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token
    ) {
        Long adminId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return ResponseEntity.ok(driverService.approve(id, adminId));
    }

    // -------- REJECT DRIVER --------
    @PutMapping("/reject/{id}")
    public ResponseEntity<String> reject(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestHeader("Authorization") String token
    ) {
        Long adminId = jwtUtil.getUserId(token.replace("Bearer ", ""));
        return ResponseEntity.ok(driverService.reject(id, reason, adminId));
    }
}
