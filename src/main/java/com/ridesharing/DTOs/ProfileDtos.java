package com.ridesharing.DTOs;

import lombok.Data;

@Data
public class ProfileDtos {
    private Long id;
    private String name;
    private String email;
    private String role;
    private Boolean enabled;
    private String driverStatus;
}
