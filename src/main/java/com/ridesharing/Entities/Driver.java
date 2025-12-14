package com.ridesharing.Entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
@Entity
@Table(name = "drivers", indexes = @Index(columnList = "email"))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // link back to User

    private String name;
    private String email;
    private String phone;

    private String vehicleModel;
    private String vehicleNumber;

    private boolean approved = false;
    private boolean online = false;

    @CreationTimestamp
    private Instant createdAt;
}
