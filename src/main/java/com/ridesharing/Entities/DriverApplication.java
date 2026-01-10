package com.ridesharing.Entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
@Entity
@Table(name = "driver_applications")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String licenseNumber;
    private String vehicleNumber;
    private String vehicleModel;

    @Enumerated(EnumType.STRING)
    private DriverStatus status = DriverStatus.PENDING;

    private Long reviewedByAdminId;
    private Instant reviewedAt;
    private String rejectionReason;


    @CreationTimestamp
    private Instant appliedAt;
}
