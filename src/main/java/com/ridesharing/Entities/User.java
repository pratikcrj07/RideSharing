package com.ridesharing.Entities;

import com.ridesharing.Entities.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
@Entity
@Table(name = "users", indexes = @Index(columnList = "email"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.ROLE_USER;

    private boolean enabled = false;
    private boolean emailVerified = false;

    @Enumerated(EnumType.STRING)
    private DriverStatus driverStatus = DriverStatus.NOT_APPLIED;

    private String provider = "LOCAL";

    @CreationTimestamp
    private Instant createdAt;
}
