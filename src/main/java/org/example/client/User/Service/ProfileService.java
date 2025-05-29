package org.example.client.User.Service;

import org.example.client.User.Repository.ProfileRepository;
import org.example.client.User.dto.Request.UpdateProfileRequest;
import org.example.client.core.common.dto.ProfileResponse;
import org.example.client.core.common.util.SessionManager;
import org.example.client.core.config.ApiConfig;
import retrofit2.Response;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class ProfileService {
    private final ProfileRepository profileRepository;

    public ProfileService() {
        this.profileRepository = ApiConfig.getRetrofit().create(ProfileRepository.class);
    }

    public CompletableFuture<ProfileResponse> getProfileAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Response<ProfileResponse> response = profileRepository.getProfile().execute();
                if (response.isSuccessful()) {
                    return response.body();
                } else {
                    throw new ProfileException("Ошибка получения профиля: " + response.errorBody().string());
                }
            } catch (IOException e) {
                throw new ProfileException("Сетевая ошибка: " + e.getMessage());
            }
        });
    }

    // Измененный метод удаления профиля
    public CompletableFuture<Boolean> deleteProfileAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Response<Void> response = profileRepository.deleteProfile().execute();
                if (response.isSuccessful()) {
                    SessionManager.clearSession();
                    return true;
                } else {
                    throw new ProfileException("Ошибка удаления: " + response.errorBody().string());
                }
            } catch (IOException e) {
                throw new ProfileException("Сетевая ошибка: " + e.getMessage());
            }
        });
    }

    // Измененный метод обновления пароля
    public CompletableFuture<Boolean> updatePasswordAsync(String currentPassword, String newPassword) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UpdateProfileRequest request = new UpdateProfileRequest(
                        currentPassword,
                        newPassword,
                        null,
                        null
                );

                Response<Void> response = profileRepository.updatePassword(request).execute();
                if (response.isSuccessful()) {
                    return true;
                } else {
                    throw new ProfileException("Ошибка смены пароля: " + response.errorBody().string());
                }
            } catch (IOException e) {
                throw new ProfileException("Сетевая ошибка: " + e.getMessage());
            }
        });
    }

    // Измененный метод обновления контактов
    public CompletableFuture<Boolean> updateContactInfoAsync(String email, String phone) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                UpdateProfileRequest request = new UpdateProfileRequest(
                        null,
                        null,
                        email,
                        phone
                );

                Response<Void> response = profileRepository.updateContactInfo(request).execute();
                if (response.isSuccessful()) {
                    return true;
                } else {
                    throw new ProfileException("Ошибка обновления контактов: " + response.errorBody().string());
                }
            } catch (IOException e) {
                throw new ProfileException("Сетевая ошибка: " + e.getMessage());
            }
        });
    }

    public static class ProfileException extends RuntimeException {
        public ProfileException(String message) {
            super(message);
        }
    }
}