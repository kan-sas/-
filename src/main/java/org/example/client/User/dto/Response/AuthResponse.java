package org.example.client.User.dto.Response;

import java.util.Objects;

public final class AuthResponse {
    private final String token;

    public AuthResponse(String token) {
        this.token = Objects.requireNonNull(token, "Token cannot be null");
    }

    public String getToken() {
        return token;
    }

    @Override
    public String toString() {
        return "JwtResponse{token='***'}";
    }
}