package com.ridesharing.Controller;

import com.ridesharing.DTOs.ProfileDTO;
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

        String principal = auth.getPrincipal().toString();

        // 1️⃣ Check User
        Optional<User> userOpt = userService.findByEmail(principal);
        if (userOpt.isPresent()) {
            User u = userOpt.get();
            ProfileDTO dto = mapToProfileDTO(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getEnabled(), u.getDriverStatus());
            return ResponseEntity.ok(dto);
        }

        // 2️⃣ Check Driver
        Optional<Driver> driverOpt = driverRepository.findByEmail(principal);
        if (driverOpt.isPresent()) {
            Driver d = driverOpt.get();
            ProfileDtos dto = mapToProfileDTO(d.getId(), d.getName(), d.getEmail(), d.getRole(), d.getEnabled(), d.getDriverStatus());
            return ResponseEntity.ok(dto);
        }

        // 3️⃣ Check Admin
        Optional<Admin> adminOpt = adminRepository.findByEmail(principal);
        if (adminOpt.isPresent()) {
            Admin a = adminOpt.get();
            ProfileDtos dto = mapToProfileDTO(a.getId(), a.getName(), a.getEmail(), a.getRole(), a.getEnabled(), null); // driverStatus N/A
            return ResponseEntity.ok(dto);
        }

        return ResponseEntity.notFound().build();
    }

    private ProfileDtos mapToProfileDTO(Long id, String name, String email, String role, Boolean enabled, String driverStatus) {
        ProfileDtos dto = new ProfileDtos();
        dto.setId(id);
        dto.setName(name);
        dto.setEmail(email);
        dto.setRole(role);
        dto.setEnabled(enabled);
        dto.setDriverStatus(driverStatus);
        return dto;
    }
}
