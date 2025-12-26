package com.ridesharing.Entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;@Entity
@Table(name = "users", indexes = @Index(columnList = "email"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    private String password;

//    @Enumerated(EnumType.STRING)
//    private Role role = Role.ROLE_USER;

    private boolean enabled;
    private boolean emailVerified;

    @Enumerated(EnumType.STRING)
    private DriverStatus driverStatus = DriverStatus.NOT_APPLIED;

    private String provider;

    @CreationTimestamp
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    private Role Role;
}
