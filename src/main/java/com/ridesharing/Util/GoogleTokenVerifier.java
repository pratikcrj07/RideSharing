package com.ridesharing.Util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class GoogleTokenVerifier {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static class Payload {
        private String email;
        private String name;
        private boolean emailVerified;

        public Payload(String email, String name, boolean emailVerified) {
            this.email = email;
            this.name = name;
            this.emailVerified = emailVerified;
        }

        public String getEmail() { return email; }
        public String getName() { return name; }
        public boolean isEmailVerified() { return emailVerified; }
    }

    public static Payload verify(String idToken) {
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + java.net.URLEncoder.encode(idToken, StandardCharsets.UTF_8);
            HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5_000);
            conn.setReadTimeout(5_000);

            try (InputStream is = conn.getInputStream()) {
                JsonNode n = MAPPER.readTree(is);
                String email = n.path("email").asText(null);
                String name = n.path("name").asText(null);
                boolean verified = n.path("email_verified").asText("false").equalsIgnoreCase("true");
                if (email == null) throw new IllegalArgumentException("invalid google token: no email");
                return new Payload(email, name, verified);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("invalid google id_token", ex);
        }
    }
}
