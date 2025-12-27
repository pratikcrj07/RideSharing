package com.ridesharing.Controller;

import com.ridesharing.DTOs.ProfileDtos;
import com.ridesharing.Entities.Admin;
import com.ridesharing.Entities.Driver;
import com.ridesharing.Entities.User;
import com.ridesharing.Repository.AdminRepository;
import com.ridesharing.Repository.DriverRepository;
import com.ridesharing.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final DriverRepository driverRepository;
    private final AdminRepository adminRepository;

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();

        String email = auth.getPrincipal().toString();

        // ========= USER =========
        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isPresent()) {
            User u = userOpt.get();

            ProfileDtos dto = new ProfileDtos();
            dto.setId(u.getId());
            dto.setName(u.getName());
            dto.setEmail(u.getEmail());
            dto.setRole(u.getRole().name());              // ENUM → String
            dto.setEnabled(u.isEnabled());                // boolean getter
            dto.setDriverStatus(
                    u.getDriverStatus() != null
                            ? u.getDriverStatus().name()
                            : null
            );

            return ResponseEntity.ok(dto);
        }
// ========= DRIVER =========
        Optional<Driver> driverOpt = driverRepository.findByEmail(email);
        if (driverOpt.isPresent()) {
            Driver d = driverOpt.get();

            ProfileDtos dto = new ProfileDtos();
            dto.setId(d.getId());
            dto.setName(d.getName());
            dto.setEmail(d.getEmail());
            dto.setRole("ROLE_DRIVER");
            dto.setEnabled(d.isApproved());

            // driverStatus does NOT belong to Driver entity
            dto.setDriverStatus(null);

            return ResponseEntity.ok(dto);
        }

        // ========= ADMIN =========
        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            Admin a = adminOpt.get();

            ProfileDtos dto = new ProfileDtos();
            dto.setId(a.getId());
            dto.setName(a.getName());
            dto.setEmail(a.getEmail());
            dto.setRole("ROLE_ADMIN");
            dto.setEnabled(true);
            dto.setDriverStatus(null);

            return ResponseEntity.ok(dto);
        }

        return ResponseEntity.notFound().build();
    }
}
