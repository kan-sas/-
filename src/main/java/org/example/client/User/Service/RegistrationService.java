package org.example.client.User.Service;

import org.example.client.User.Repository.AuthRepository;
import org.example.client.User.dto.Request.RegistrationRequest;
import org.example.client.core.config.ApiConfig;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.concurrent.CompletableFuture;

public class RegistrationService {
    private final AuthRepository authRepository;

    public RegistrationService() {
        this.authRepository = ApiConfig.getRetrofit().create(AuthRepository.class);
    }

    public CompletableFuture<Void> registerAsync(RegistrationRequest request) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        authRepository.register(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(new RegistrationException(
                            "Registration failed: " + response.code(), response.code())
                    );
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                future.completeExceptionally(new RegistrationException("Network error", t));
            }
        });

        return future;
    }

    public static class RegistrationException extends Exception {
        private final int statusCode;

        public RegistrationException(String message, int statusCode) {
            super(message);
            this.statusCode = statusCode;
        }

        public RegistrationException(String message, Throwable cause) {
            super(message, cause);
            this.statusCode = -1;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}