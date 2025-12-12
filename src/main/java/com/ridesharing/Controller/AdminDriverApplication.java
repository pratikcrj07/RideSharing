package com.ridesharing.Controller;

import com.ridesharing.Services.DriverApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/driver")
@RequiredArgsConstructor
public class AdminDriverApplication {

    private final DriverApplicationService driverService;

    @PutMapping("/approve/{id}")
    public ResponseEntity<String> approve(@PathVariable Long id) {
        return ResponseEntity.ok(driverService.approve(id));
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<String> reject(@PathVariable Long id) {
        return ResponseEntity.ok(driverService.reject(id));
    }
}
