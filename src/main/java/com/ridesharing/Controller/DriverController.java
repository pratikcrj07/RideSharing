package com.ridesharing.Controller;

import com.ridesharing.Entities.DriverApplication;
import com.ridesharing.Services.DriverApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverController {

    private final DriverApplicationService driverService;

    @PostMapping("/apply")
    public ResponseEntity<String> apply(@RequestParam Long userId,
                                        @RequestBody DriverApplication req) {
        return ResponseEntity.ok(driverService.apply(userId, req));
    }
}
