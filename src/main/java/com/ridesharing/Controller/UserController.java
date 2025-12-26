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

    // If you have DriverRepository or AdminRepository, inject them here:
    private final DriverRepository driverRepository;
    private final AdminRepository adminRepository;

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();

        String principal = auth.getPrincipal().toString();  // can be ID or email

        // Try parsing as Long (old logic)
        try {
            Long id = Long.parseLong(principal);
            Optional<User> u = userService.findByIdOptional(id);
            if (u.isPresent()) {
                u.get().setPassword(null);
                return ResponseEntity.ok(u.get());
            }
        } catch (NumberFormatException e) {
            // Not a number, fallback to email
        }

        // Fallback to email search across all tables
        String email = principal;
        Optional<User> user = userService.findByEmail(email);
        if (user.isPresent()) {
            user.get().setPassword(null);
            return ResponseEntity.ok(user.get());
        }

        Optional<Driver> driver = driverRepository.findByEmail(email);
        if (driver.isPresent()) return ResponseEntity.ok(driver.get());

        Optional<Admin> admin = adminRepository.findByEmail(email);
        if (admin.isPresent()) return ResponseEntity.ok(admin.get());

        return ResponseEntity.notFound().build();
    }
}
