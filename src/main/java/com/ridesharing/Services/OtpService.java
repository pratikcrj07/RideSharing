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
    private static final Duration TTL = Duration.ofMinutes(5);

    private String keyFor(String email) {
        return "OTP:" + email.toLowerCase();
    }

    public String generateAndStoreOtp(String email) {
        int code = 100000 + random.nextInt(900000);
        String otp = String.valueOf(code);
        redisTemplate.opsForValue().set(keyFor(email), otp, TTL);
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        String stored = redisTemplate.opsForValue().get(keyFor(email));
        return stored != null && stored.equals(otp);
    }

    public void removeOtp(String email) {
        redisTemplate.delete(keyFor(email));
    }
}
