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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // ---------------- USER REGISTER ----------------
    @Transactional
    public User registerUser(RegisterRequest r) {
        String email = r.getEmail().toLowerCase();

        if (userRepository.existsByEmail(email))
            throw new ApiException("Email already used");

        User user = User.builder()
                .name(r.getName())
                .email(email)
                .phone(r.getPhone())
                .password(passwordEncoder.encode(r.getPassword()))
                .role(Role.ROLE_USER)
                .provider("LOCAL")
                .enabled(true)
                .emailVerified(false)
                .driverStatus(DriverStatus.NOT_APPLIED)
                .build();

        User saved = userRepository.save(user);
        kafkaTemplate.send("auth-events", "USER_REGISTERED:" + saved.getId());
        return saved;
    }

    // ---------------- ADMIN REGISTER ----------------
    @Transactional
    public Admin registerAdmin(RegisterRequest r) {
        String email = r.getEmail().toLowerCase();

        if (adminRepository.existsByEmail(email))
            throw new ApiException("Email already used");

        Admin admin = Admin.builder()
                .name(r.getName())
                .email(email)
                .password(passwordEncoder.encode(r.getPassword()))
                .role(Role.ROLE_ADMIN)
                .createdAt(Instant.now())
                .build();

        Admin saved = adminRepository.save(admin);
        kafkaTemplate.send("auth-events", "ADMIN_REGISTERED:" + saved.getId());
        return saved;
    }

    // ---------------- LOGIN ----------------
    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail().toLowerCase();

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent() &&
                passwordEncoder.matches(req.getPassword(), userOpt.get().getPassword())) {

            User user = userOpt.get();
            return generateTokens(user.getId(), user.getRole().name());
        }

        Optional<Admin> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent() &&
                passwordEncoder.matches(req.getPassword(), adminOpt.get().getPassword())) {

            Admin admin = adminOpt.get();
            return generateTokens(admin.getId(), admin.getRole().name());
        }

        throw new ApiException("Invalid email or password");
    }

    // ---------------- OTP LOGIN ----------------
    public AuthResponse verifyOtp(OtpVerifyRequest req) {
        String email = req.getEmail().toLowerCase();

        if (!otpService.validateOtp(email, req.getOtp()))
            throw new ApiException("Invalid or expired OTP");

        User user = userRepository.findByEmail(email).orElseGet(() ->
                userRepository.save(User.builder()
                        .name("OTP_USER")
                        .email(email)
                        .provider("OTP")
                        .role(Role.ROLE_USER)
                        .enabled(true)
                        .emailVerified(true)
                        .driverStatus(DriverStatus.NOT_APPLIED)
                        .build())
        );

        otpService.removeOtp(email);
        return generateTokens(user.getId(), user.getRole().name());
    }

    // ---------------- GOOGLE LOGIN ----------------
    public AuthResponse googleLogin(String idToken) {
        GoogleTokenVerifier.Payload payload = GoogleTokenVerifier.verify(idToken);
        String email = payload.getEmail().toLowerCase();

        User user = userRepository.findByEmail(email).orElseGet(() ->
                userRepository.save(User.builder()
                        .name(payload.getName())
                        .email(email)
                        .provider("GOOGLE")
                        .emailVerified(true)
                        .enabled(true)
                        .role(Role.ROLE_USER)
                        .driverStatus(DriverStatus.NOT_APPLIED)
                        .build())
        );

        return generateTokens(user.getId(), user.getRole().name());
    }

    // ---------------- TOKEN ----------------
    private AuthResponse generateTokens(Long userId, String role) {
        String access = jwtUtil.generateToken(userId, role);
        String refresh = jwtUtil.generateRefreshToken(userId, role);
        kafkaTemplate.send("auth-events", "LOGIN:" + userId);
        return new AuthResponse(access, refresh);
    }
}
