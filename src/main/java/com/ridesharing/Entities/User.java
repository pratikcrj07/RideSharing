package com.ridesharing.Entities;

import com.ridesharing.Entities.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users", indexes = {@Index(columnList = "email")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @Column(nullable = true)
    private String password; // BCrypt hashed; null for OTP/google users

    @Enumerated(EnumType.STRING)
    private Role role = Role.ROLE_USER;

    private boolean enabled = true;

    private Instant createdAt = Instant.now();

    private boolean emailVerified = false;

    // provider: LOCAL, GOOGLE, OTP
    private String provider = "LOCAL";
}
