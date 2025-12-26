package com.ridesharing.Controller;

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
    private final DriverRepository driverRepository; // inject if exists
    private final AdminRepository adminRepository;   // inject if exists

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();

        String principal = auth.getPrincipal().toString();

        // 1️⃣ Try old numeric ID flow first
        User userById = null;
        try {
            Long id = Long.parseLong(principal);
            userById = userService.findById(id);
            if (userById != null) {
                userById.setPassword(null);
                return ResponseEntity.ok(userById);
            }
        } catch (NumberFormatException ignored) {
            // principal is not a number → fallback to email
        }

        // 2️⃣ Fallback: treat principal as email
        String email = principal;

        Optional<User> userOpt = userService.findByEmail(email);
        if (userOpt.isPresent()) {
            User u = userOpt.get();
            u.setPassword(null);
            return ResponseEntity.ok(u);
        }

        Optional<Driver> driverOpt = driverRepository.findByEmail(email);
        if (driverOpt.isPresent()) return ResponseEntity.ok(driverOpt.get());

        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) return ResponseEntity.ok(adminOpt.get());

        return ResponseEntity.notFound().build();
    }
}