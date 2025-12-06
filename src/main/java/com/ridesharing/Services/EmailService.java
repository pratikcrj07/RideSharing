package com.ridesharing.Services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendOtp(String email, String otp) {

        String url = "https://api.brevo.com/v3/smtp/email";

        Map<String, Object> payload = Map.of(
                "sender", Map.of("name", "RideSharing", "email", "info@ridesharing.com"),
                "to", new Object[]{ Map.of("email", email) },
                "subject", "Your RideSharing OTP",
                "htmlContent", "<h1>Your OTP is: " + otp + "</h1>"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<?> entity = new HttpEntity<>(payload, headers);

        restTemplate.postForEntity(url, entity, String.class);
    }
}
