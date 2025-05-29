package org.example.client.User.Service;

import org.example.client.User.Repository.AuthRepository;
import org.example.client.User.dto.Request.LoginRequest;
import org.example.client.User.dto.Response.AuthResponse;
import org.example.client.core.common.util.SessionManager;
import org.example.client.core.config.ApiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.concurrent.CompletableFuture;

public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final AuthRepository authRepository;

    public AuthService() {
        this.authRepository = ApiConfig.getRetrofit().create(AuthRepository.class);
        log.debug("AuthService initialized with AuthRepository: {}", authRepository);
    }

    public CompletableFuture<AuthResponse> loginAsync(LoginRequest request) {
        CompletableFuture<AuthResponse> future = new CompletableFuture<>();
        log.info("Attempting login for username='{}'", request.getUsername());

        authRepository.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                log.debug("Login response received: HTTP {}", response.code());
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    log.info("Login successful, token received: {}", authResponse.getToken());
                    try {
                        // Сохраняем сессию только с токеном
                        SessionManager.initSession(authResponse.getToken());
                        log.debug("Session initialized with token");
                        future.complete(authResponse);
                    } catch (Exception e) {
                        log.error("Failed to process token", e);
                        future.completeExceptionally(new AuthException("Failed to process token", e));
                    }
                } else {
                    log.warn("Login failed with status code {} and message {}", response.code(), response.message());
                    future.completeExceptionally(new AuthException(
                            "Login failed: " + response.code(), response.code())
                    );
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                log.error("Network error during login", t);
                future.completeExceptionally(new AuthException("Network error", t));
            }
        });

        return future;
    }

    public static class AuthException extends Exception {
        private final int statusCode;

        public AuthException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
            log.error("AuthException thrown: {} (status code={})", message, statusCode);
        }

        public AuthException(String message, Throwable cause) {
            super(message, cause);
            this.statusCode = -1;
            log.error("AuthException thrown: {}", message, cause);
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}
