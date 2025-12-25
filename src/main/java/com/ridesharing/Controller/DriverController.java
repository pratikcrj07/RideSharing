package com.ridesharing.Controller;

import com.ridesharing.Entities.DriverApplication;
import com.ridesharing.Security.JwtUtil;
import com.ridesharing.Services.DriverApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/driver")
@RequiredArgsConstructor
public class DriverController {

    private final DriverApplicationService driverService;
    private final JwtUtil jwtUtil;

    @PostMapping("/apply")
    public ResponseEntity<String> apply(@RequestHeader("Authorization") String token,
                                        @RequestBody DriverApplication req) {

        Long userId = jwtUtil.getUserId(token.replace("Bearer ", ""));

        return ResponseEntity.ok(driverService.apply(userId, req));
    }

}
