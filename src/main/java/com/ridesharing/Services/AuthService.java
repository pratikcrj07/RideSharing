package com.ridesharing.Services;

import com.ridesharing.DTOs.*;
import com.ridesharing.Entities.Admin;
import com.ridesharing.Entities.Driver;
import com.ridesharing.Entities.Role;
import com.ridesharing.Entities.User;
import com.ridesharing.Exception.ApiException;
import com.ridesharing.Repository.AdminRepository;
import com.ridesharing.Repository.DriverRepository;
import com.ridesharing.Repository.UserRepository;
import com.ridesharing.Security.JwtUtil;
import com.ridesharing.Util.GoogleTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final DriverRepository driverRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final KafkaTemplate<String, String> kafkaTemplate;

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
                .emailVerified(false)
                .enabled(true)
                .build();

        User saved = userRepository.save(user);
        kafkaTemplate.send("auth-events", "USER_REGISTERED:" + saved.getEmail());
        return saved;
    }

    @Transactional
    public Driver registerDriver(RegisterRequest r) {
        String email = r.getEmail().toLowerCase();
        if (driverRepository.existsByEmail(email))
            throw new ApiException("Email already used");

        Driver d = Driver.builder()
                .name(r.getName())
                .email(email)
                .phone(r.getPhone())
                .password(passwordEncoder.encode(r.getPassword()))
                .role(Role.ROLE_DRIVER)
                .provider("LOCAL")
                .approved(false)
                .build();

        Driver saved = driverRepository.save(d);
        kafkaTemplate.send("auth-events", "DRIVER_REGISTERED:" + saved.getEmail());
        return saved;
    }

    @Transactional
    public Admin registerAdmin(RegisterRequest r) {
        String email = r.getEmail().toLowerCase();
        if (adminRepository.existsByEmail(email))
            throw new ApiException("Email already used");

        Admin a = Admin.builder()
                .name(r.getName())
                .email(email)
                .password(passwordEncoder.encode(r.getPassword()))
                .role(Role.ROLE_ADMIN)
                .build();

        Admin saved = adminRepository.save(a);
        kafkaTemplate.send("auth-events", "ADMIN_REGISTERED:" + saved.getEmail());
        return saved;
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail().toLowerCase();

        // Check User
        Optional<User> u = userRepository.findByEmail(email);
        if (u.isPresent() && passwordEncoder.matches(req.getPassword(), u.get().getPassword()))
            return generateTokens(u.get().getEmail(), u.get().getRole().name());

        // Check  the Driver
        Optional<Driver> d = driverRepository.findByEmail(email);
        if (d.isPresent() && passwordEncoder.matches(req.getPassword(), d.get().getPassword()))
            return generateTokens(d.get().getEmail(), d.get().getRole().name());

        // Check Admin
        Optional<Admin> a = adminRepository.findByEmail(email);
        if (a.isPresent() && passwordEncoder.matches(req.getPassword(), a.get().getPassword()))
            return generateTokens(a.get().getEmail(), a.get().getRole().name());

        throw new ApiException("Invalid email or password");
    }

    private AuthResponse generateTokens(String email, String role) {
        String access = jwtUtil.generateToken(email, role);
        String refresh = jwtUtil.generateRefreshToken(email, role);
        kafkaTemplate.send("auth-events", "LOGIN:" + email);
        return new AuthResponse(access, refresh);
    }

    public AuthResponse verifyOtp(OtpVerifyRequest req) {
        String email = req.getEmail().toLowerCase();

        if (!otpService.validateOtp(email, req.getOtp()))
            throw new ApiException("Invalid or expired OTP");

        // Automatically create the OTP_USER if not exists
        User user = userRepository.findByEmail(email).orElseGet(() ->
                userRepository.save(User.builder()
                        .name("OTP_USER")
                        .email(email)
                        .provider("OTP")
                        .role(Role.ROLE_USER)
                        .emailVerified(true)
                        .enabled(true)
                        .build())
        );

        otpService.removeOtp(email);

        return generateTokens(user.getEmail(), user.getRole().name());
    }

    public AuthResponse googleLogin(String idToken) {
        GoogleTokenVerifier.Payload p = GoogleTokenVerifier.verify(idToken);
        String email = p.getEmail().toLowerCase();

        User user = userRepository.findByEmail(email).orElseGet(() ->
                userRepository.save(User.builder()
                        .name(p.getName())
                        .email(email)
                        .provider("GOOGLE")
                        .emailVerified(true)
                        .role(Role.ROLE_USER)
                        .enabled(true)
                        .build())
        );

        return generateTokens(user.getEmail(), user.getRole().name());
    }
}
