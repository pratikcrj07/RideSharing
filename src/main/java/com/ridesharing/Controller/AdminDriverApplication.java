package com.ridesharing.Controller;

import com.ridesharing.Entities.DriverApplication;
import com.ridesharing.Security.JwtUtil;
import com.ridesharing.Services.DriverApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/driver-applications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminDriverApplicationController {

    private final DriverApplicationService service;
    private final JwtUtil jwtUtil;

    @GetMapping
    public ResponseEntity<List<DriverApplication>> getAll() {
        return ResponseEntity.ok(service.getAllApplications());
    }

    @GetMapping("/pending")
    public ResponseEntity<List<DriverApplication>> getPending() {
        return ResponseEntity.ok(service.getPendingApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriverApplication> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.getApplication(id));
    }

    @PutMapping("/{id}/approve")
    public ResponseEntity<String> approve(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token
    ) {
        Long adminId = jwtUtil.getUserId(token.substring(7));
        return ResponseEntity.ok(service.approve(id, adminId));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<String> reject(
            @PathVariable Long id,
            @RequestParam String reason,
            @RequestHeader("Authorization") String token
    ) {
        Long adminId = jwtUtil.getUserId(token.substring(7));
        return ResponseEntity.ok(service.reject(id, reason, adminId));
    }
}
