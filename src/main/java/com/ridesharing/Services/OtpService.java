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

    private static final Duration TTL = Duration.ofMinutes(5);           // OTP valid 5 min
    private static final Duration RATE_LIMIT_TTL = Duration.ofMinutes(1); // Limit one OTP per minute

    private String keyFor(String email) {
        return "OTP:" + email.toLowerCase();
    }

    private String rateLimitKey(String email) {
        return "OTP_RATE:" + email.toLowerCase();
    }

    // Generate OTP with rate limiting
    public String generateAndStoreOtp(String email) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(rateLimitKey(email)))) {
            throw new RuntimeException("Please wait before requesting another OTP");
        }

        int code = 100000 + random.nextInt(900000);
        String otp = String.valueOf(code);

        redisTemplate.opsForValue().set(keyFor(email), otp, TTL);
        redisTemplate.opsForValue().set(rateLimitKey(email), "1", RATE_LIMIT_TTL);

        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        String stored = redisTemplate.opsForValue().get(keyFor(email));
        boolean valid = stored != null && stored.equals(otp);
        if (valid) removeOtp(email); // auto-remove on successful validation
        return valid;
    }

    public void removeOtp(String email) {
        redisTemplate.delete(keyFor(email));
        redisTemplate.delete(rateLimitKey(email));
    }
}
