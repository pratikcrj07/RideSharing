package com.ridesharing.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long driverId;

    private double pickupLat;
    private double pickupLng;
    private double dropLat;
    private double dropLng;

    private String status;

    private double fare;

    private Instant createdAt = Instant.now();
}
