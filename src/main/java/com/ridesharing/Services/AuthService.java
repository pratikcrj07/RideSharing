package com.ridesharing.Services;

import com.ridesharing.DTOs.*;
import com.ridesharing.Entities.*;
import com.ridesharing.Exception.ApiException;
import com.ridesharing.Repository.AdminRepository;
import com.ridesharing.Repository.UserRepository;
import com.ridesharing.Security.JwtUtil;
import com.ridesharing.Util.GoogleTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // ================= USER REGISTER =================
    @Transactional
    public void registerUser(RegisterRequest r) {
        String email = r.getEmail().toLowerCase();

        if (userRepository.existsByEmail(email))
            throw new ApiException("Email already used");

        User user = userRepository.save(User.builder()
                .name(r.getName())
                .email(email)
                .phone(r.getPhone())
                .password(passwordEncoder.encode(r.getPassword()))
                .role(Role.ROLE_USER)
                .provider("LOCAL")
                .enabled(false)
                .emailVerified(false)
                .driverStatus(DriverStatus.NOT_APPLIED)
                .build());

        otpService.sendOtp(email);
        kafkaTemplate.send("auth-events", "USER_REGISTERED:" + user.getId());
    }


    // ================= ADMIN REGISTER =================
    @Transactional
    public Admin registerAdmin(RegisterRequest r) {
        String email = r.getEmail().toLowerCase();

        if (adminRepository.existsByEmail(email))
            throw new ApiException("Email already used");

        Admin admin = adminRepository.save(Admin.builder()
                .name(r.getName())
                .email(email)
                .password(passwordEncoder.encode(r.getPassword()))
                .role(Role.ROLE_ADMIN)
                .createdAt(Instant.now())
                .build());

        kafkaTemplate.send("auth-events", "ADMIN_REGISTERED:" + admin.getId());
        return admin;
    }

    // ================= PASSWORD LOGIN =================
    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail().toLowerCase();

        return userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(req.getPassword(), u.getPassword()))
                .map(u -> generateTokens(u.getId(), u.getRole().name()))
                .orElseGet(() ->
                        adminRepository.findByEmail(email)
                                .filter(a -> passwordEncoder.matches(req.getPassword(), a.getPassword()))
                                .map(a -> generateTokens(a.getId(), a.getRole().name()))
                                .orElseThrow(() -> new ApiException("Invalid credentials"))
                );
    }

    // ================= OTP LOGIN =================
    public AuthResponse loginWithOtp(OtpVerifyRequest req) {
        String email = req.getEmail().toLowerCase();

        if (!otpService.validateOtp(email, req.getOtp()))
            throw new ApiException("Invalid or expired OTP");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .name("OTP_USER")
                        .provider("OTP")
                        .role(Role.ROLE_USER)
                        .enabled(true)
                        .emailVerified(true)
                        .driverStatus(DriverStatus.NOT_APPLIED)
                        .build()));

        otpService.removeOtp(email);
        return generateTokens(user.getId(), user.getRole().name());
    }

    // ================= GOOGLE LOGIN =================
    public AuthResponse googleLogin(String idToken) {
        GoogleTokenVerifier.Payload payload = GoogleTokenVerifier.verify(idToken);
        String email = payload.getEmail().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .name(payload.getName())
                        .email(email)
                        .provider("GOOGLE")
                        .emailVerified(true)
                        .enabled(true)
                        .role(Role.ROLE_USER)
                        .driverStatus(DriverStatus.NOT_APPLIED)
                        .build()));

        return generateTokens(user.getId(), user.getRole().name());
    }

    private AuthResponse generateTokens(Long id, String role) {
        kafkaTemplate.send("auth-events", "LOGIN:" + id);
        return new AuthResponse(
                jwtUtil.generateToken(id, role),
                jwtUtil.generateRefreshToken(id, role)
        );
    }
}
