package com.ridesharing.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SecureRandom random = new SecureRandom();

    private static final Duration OTP_TTL = Duration.ofMinutes(5);
    private static final Duration RATE_LIMIT_TTL = Duration.ofMinutes(1);

    private String otpKey(String email) {
        return "OTP:" + email.toLowerCase();
    }

    private String rateKey(String email) {
        return "OTP_RATE:" + email.toLowerCase();
    }

    public String generateAndStoreOtp(String email) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateKey(email)))) {
            throw new RuntimeException("OTP request too frequent");
        }

        String otp = String.valueOf(100000 + random.nextInt(900000));

        redisTemplate.opsForValue().set(otpKey(email), otp, OTP_TTL);
        redisTemplate.opsForValue().set(rateKey(email), "1", RATE_LIMIT_TTL);

        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        String stored = redisTemplate.opsForValue().get(otpKey(email));
        return stored != null && stored.equals(otp);
    }

    public void removeOtp(String email) {
        redisTemplate.delete(otpKey(email));
        redisTemplate.delete(rateKey(email));
    }
}
