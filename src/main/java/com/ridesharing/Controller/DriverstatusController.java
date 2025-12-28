package com.ridesharing.Controller;


import com.ridesharing.Entities.DriverStatus;
import com.ridesharing.Services.DriverstatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/drivers")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class DriverstatusController {

    private final DriverstatusService driverAdminService;

    @PutMapping("/{driverId}/suspend")
    public ResponseEntity<String> suspend(@PathVariable Long driverId) {
        return ResponseEntity.ok(
                driverAdminService.suspendDriver(driverId)
        );
    }

    @PutMapping("/{driverId}/activate")
    public ResponseEntity<String> activate(@PathVariable Long driverId) {
        return ResponseEntity.ok(
                driverAdminService.activateDriver(driverId)
        );
    }
}
