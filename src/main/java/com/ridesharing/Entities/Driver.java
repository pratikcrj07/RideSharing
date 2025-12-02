package com.ridesharing.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "drivers", indexes = {@Index(columnList = "email")})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @Column(nullable = true)
    private String password; // hashed, may be null for OAuth

    @Enumerated(EnumType.STRING)
    private Role role = Role.ROLE_DRIVER;

    private boolean approved = false;
    private boolean online = false;
    private Instant createdAt = Instant.now();

    private String vehicleModel;
    private String vehicleNumber;
    private String provider = "LOCAL";
}
