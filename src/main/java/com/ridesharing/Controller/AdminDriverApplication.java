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
public class AdminDriverApplication {

    private final DriverApplicationService service;
    private final JwtUtil jwtUtil;

    //  Only pending applications
    @GetMapping("/pending")
    public ResponseEntity<List<DriverApplication>> getPending() {
        return ResponseEntity.ok(service.getPendingApplications());
    }

    //  Approve
    @PutMapping("/{id}/approve")
    public ResponseEntity<String> approve(
            @PathVariable Long id,
            @RequestHeader("Authorization") String token
    ) {
        Long adminId = jwtUtil.getUserId(token.substring(7));
        service.approve(id, adminId);
        return ResponseEntity.ok("Approved");
    }

    //  Reject with short feedback
    @PutMapping("/{id}/reject")
    public ResponseEntity<String> reject(
            @PathVariable Long id,
            @RequestParam String feedback,
            @RequestHeader("Authorization") String token
    ) {
        Long adminId = jwtUtil.getUserId(token.substring(7));
        service.reject(id, feedback, adminId);
        return ResponseEntity.ok("Rejected");
    }
}
