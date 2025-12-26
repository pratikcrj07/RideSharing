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
    private final DriverRepository driverRepository;
    private final AdminRepository adminRepository;

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();

        String principal = auth.getPrincipal().toString();

        // 1️⃣ Try as User
        Optional<User> userOpt = userService.findByEmail(principal);
        if (userOpt.isPresent()) {
            User u = userOpt.get();
            u.setPassword(null);  // hide password
            return ResponseEntity.ok(u);
        }

        // 2️⃣ Try as Driver
        Optional<Driver> driverOpt = driverRepository.findByEmail(principal);
        if (driverOpt.isPresent()) {
            Driver d = driverOpt.get();
            d.setPassword(null);
            return ResponseEntity.ok(d);
        }

        // 3️⃣ Try as Admin
        Optional<Admin> adminOpt = adminRepository.findByEmail(principal);
        if (adminOpt.isPresent()) {
            Admin a = adminOpt.get();
            a.setPassword(null);
            return ResponseEntity.ok(a);
        }

        return ResponseEntity.notFound().build();
    }
}
