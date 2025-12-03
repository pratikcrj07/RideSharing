package com.ridesharing.Services;

import com.ridesharing.DTOs.*;
import com.ridesharing.Entities.Admin;
import com.ridesharing.Entities.Driver;
import com.ridesharing.Entities.Role;
import com.ridesharing.Entities.User;
import com.ridesharing.Repository.AdminRepository;
import com.ridesharing.Repository.DriverRepository;
import com.ridesharing.Repository.UserRepository;
import com.ridesharing.Security.JwtUtil;
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
    private final KafkaTemplate<String, String> kafkaTemplate; // optional: event logs

    @Transactional
    public User registerUser(RegisterRequest r) {
        if (userRepository.existsByEmail(r.getEmail())) {
            throw new IllegalArgumentException("email already used");
        }
        User u = User.builder()
                .name(r.getName())
                .email(r.getEmail().toLowerCase())
                .phone(r.getPhone())
                .password(passwordEncoder.encode(r.getPassword()))
                .role(Role.ROLE_USER)
                .provider("LOCAL")
                .emailVerified(false)
                .build();
        User saved = userRepository.save(u);
        kafkaTemplate.send("auth-events", "USER_REGISTERED:" + saved.getEmail());
        return saved;
    }

    @Transactional
    public Driver registerDriver(RegisterRequest r) {
        if (driverRepository.existsByEmail(r.getEmail())) {
            throw new IllegalArgumentException("email already used");
        }
        Driver d = Driver.builder()
                .name(r.getName())
                .email(r.getEmail().toLowerCase())
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
        if (adminRepository.findByEmail(r.getEmail()).isPresent()) {
            throw new IllegalArgumentException("email already used");
        }
        Admin a = Admin.builder()
                .name(r.getName())
                .email(r.getEmail().toLowerCase())
                .password(passwordEncoder.encode(r.getPassword()))
                .role(Role.ROLE_ADMIN)
                .build();
        Admin saved = adminRepository.save(a);
        kafkaTemplate.send("auth-events", "ADMIN_REGISTERED:" + saved.getEmail());
        return saved;
    }

    public AuthResponse login(LoginRequest req) {
        String email = req.getEmail().toLowerCase();

        // try user
        Optional<User> u = userRepository.findByEmail(email);
        if (u.isPresent() && u.get().getPassword() != null
                && passwordEncoder.matches(req.getPassword(), u.get().getPassword())) {
            String access = jwtUtil.generateToken(u.get().getEmail(), u.get().getRole().name());
            String refresh = jwtUtil.generateRefreshToken(u.get().getEmail(), u.get().getRole().name());
            kafkaTemplate.send("auth-events", "LOGIN_USER:" + email);
            return new AuthResponse(access, refresh);
        }

        // try driver
        Optional<Driver> d = driverRepository.findByEmail(email);
        if (d.isPresent() && d.get().getPassword() != null
                && passwordEncoder.matches(req.getPassword(), d.get().getPassword())) {
            String access = jwtUtil.generateToken(d.get().getEmail(), d.get().getRole().name());
            String refresh = jwtUtil.generateRefreshToken(d.get().getEmail(), d.get().getRole().name());
            kafkaTemplate.send("auth-events", "LOGIN_DRIVER:" + email);
            return new AuthResponse(access, refresh);
        }

        // try admin
        Optional<Admin> a = adminRepository.findByEmail(email);
        if (a.isPresent() && a.get().getPassword() != null
                && passwordEncoder.matches(req.getPassword(), a.get().getPassword())) {
            String access = jwtUtil.generateToken(a.get().getEmail(), a.get().getRole().name());
            String refresh = jwtUtil.generateRefreshToken(a.get().getEmail(), a.get().getRole().name());
            kafkaTemplate.send("auth-events", "LOGIN_ADMIN:" + email);
            return new AuthResponse(access, refresh);
        }

        throw new IllegalArgumentException("invalid credentials");
    }

    // OTP flows (generate + verify)
    public String sendOtp(OtpRequest req) {
        String email = req.getEmail().toLowerCase();
        String otp = otpService.generateAndStoreOtp(email);
        // TODO: integrate with SMS/Email provider — currently we just return OTP (dev only)
        kafkaTemplate.send("auth-events", "OTP_SENT:" + email);
        return otp;
    }

    public AuthResponse verifyOtp(OtpVerifyRequest req) {
        String email = req.getEmail().toLowerCase();
        if (!otpService.validateOtp(email, req.getOtp())) {
            throw new IllegalArgumentException("invalid otp");
        }

        // if user exists, issue token; else create OTP user
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User nu = User.builder()
                    .name("OTP_USER")
                    .email(email)
                    .provider("OTP")
                    .role(Role.ROLE_USER)
                    .emailVerified(true)
                    .build();
            return userRepository.save(nu);
        });

        otpService.removeOtp(email);
        String access = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String refresh = jwtUtil.generateRefreshToken(user.getEmail(), user.getRole().name());
        kafkaTemplate.send("auth-events", "OTP_VERIFIED:" + email);
        return new AuthResponse(access, refresh);
    }

    // Google id_token verification flow — front end must POST idToken
    public AuthResponse googleLogin(String idToken) {
        // Use GoogleTokenVerifier (below) to verify and obtain email/name — throw on failure
        GoogleTokenVerifier.Payload payload = GoogleTokenVerifier.verify(idToken);
        String email = payload.getEmail().toLowerCase();
        String name = payload.getName();

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User nu = User.builder()
                    .name(name != null ? name : "GoogleUser")
                    .email(email)
                    .provider("GOOGLE")
                    .role(Role.ROLE_USER)
                    .emailVerified(true)
                    .build();
            return userRepository.save(nu);
        });

        String access = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        String refresh = jwtUtil.generateRefreshToken(user.getEmail(), user.getRole().name());
        kafkaTemplate.send("auth-events", "GOOGLE_LOGIN:" + email);
        return new AuthResponse(access, refresh);
    }
}
