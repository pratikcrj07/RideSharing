package com.ridesharing.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GoogleAuthRequest {
    @NotBlank
    private String idToken; // front-end posts Google ID token obtained after Google sign-in
}
