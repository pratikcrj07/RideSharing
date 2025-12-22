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
    private final EmailService emailService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // ================= USER REGISTER =================
    @Transactional
    public void registerUser(RegisterRequest req) {
        String email = req.getEmail().toLowerCase();

        if (userRepository.existsByEmail(email))
            throw new ApiException("Email already used");

        // Save user as disabled first
        User user = userRepository.save(User.builder()
                .name(req.getName())
                .email(email)
                .phone(req.getPhone())
                .password(passwordEncoder.encode(req.getPassword()))
                .role(Role.ROLE_USER)
                .provider("LOCAL")
                .enabled(false)
                .emailVerified(false)
                .driverStatus(DriverStatus.NOT_APPLIED)
                .build());

        // Generate OTP and send email
        String otp = otpService.generateAndStoreOtp(email);
        emailService.sendOtp(email, otp);

        kafkaTemplate.send("auth-events", "USER_REGISTERED:" + user.getId());
    }

    // ================= OTP VERIFY =================
    @Transactional
    public void verifyOtp(OtpVerifyRequest req) {
        String email = req.getEmail().toLowerCase();

        if (!otpService.validateOtp(email, req.getOtp()))
            throw new ApiException("Invalid or expired OTP");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found"));

        user.setEnabled(true);
        user.setEmailVerified(true);

        otpService.removeOtp(email);
    }

    // ================= LOGIN =================
    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElse(null);

        if (user != null) {
            if (!user.isEnabled())
                throw new ApiException("Email not verified");

            if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
                throw new ApiException("Invalid credentials");

            return generateTokens(user.getId(), user.getRole().name());
        }

        // Check admin login
        Admin admin = adminRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), admin.getPassword()))
            throw new ApiException("Invalid credentials");

        return generateTokens(admin.getId(), admin.getRole().name());
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

    // ================= GENERATE TOKENS =================
    private AuthResponse generateTokens(Long id, String role) {
        kafkaTemplate.send("auth-events", "LOGIN:" + id);
        return new AuthResponse(
                jwtUtil.generateToken(id, role),
                jwtUtil.generateRefreshToken(id, role)
        );
    }
}
