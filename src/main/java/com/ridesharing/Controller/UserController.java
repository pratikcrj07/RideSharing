package com.ridesharing.Controller;

import com.ridesharing.Entities.User;
import com.ridesharing.Services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> me(Authentication auth) {
        if (auth == null) return ResponseEntity.status(401).build();
        String principal = (String) auth.getPrincipal();
        return userService.findByEmail(principal)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
