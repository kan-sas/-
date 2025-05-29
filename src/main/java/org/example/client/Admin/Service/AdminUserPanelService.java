package org.example.client.Admin.Service;

import javafx.application.Platform;
import org.example.client.Admin.Repository.AdminUserRepository;
import org.example.client.Admin.dto.Request.ChangeRoleRequest;
import org.example.client.core.common.dto.ErrorResponse;
import org.example.client.core.common.dto.ProfileResponse;
import org.example.client.core.common.util.SessionManager;
import org.example.client.core.config.ApiConfig;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AdminUserPanelService {
    private final AdminUserRepository adminUserRepository;

    public AdminUserPanelService() {
        this.adminUserRepository = ApiConfig.getRetrofit().create(AdminUserRepository.class);
    }

    // Получение всех пользователей (асинхронно)
    public void fetchAllUsersAsync(Consumer<List<ProfileResponse>> onSuccess,
                                   Consumer<String> onError) {
        if (!checkAuthentication(onError)) return;

        String token = getAuthHeader();
        Call<List<ProfileResponse>> call = adminUserRepository.getAllUsers(token);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<List<ProfileResponse>> call, Response<List<ProfileResponse>> response) {
                handleResponse(response, onSuccess, onError, "Ошибка получения пользователей");
            }

            @Override
            public void onFailure(Call<List<ProfileResponse>> call, Throwable t) {
                handleFailure(t, onError);
            }
        });
    }

    // Обновление роли пользователя (асинхронно)
    public void updateUserRoleAsync(Long userId, String newRole,
                                    Consumer<ProfileResponse> onSuccess,
                                    Consumer<String> onError) {
        if (!checkAuthentication(onError)) return;

        String token = getAuthHeader();
        ChangeRoleRequest request = new ChangeRoleRequest(newRole);

        System.out.println("Отправка запроса на обновление роли: "
                + "UserID=" + userId
                + ", NewRole=" + newRole
                + ", Token=" + token);

        Call<ProfileResponse> call = adminUserRepository.changeUserRole(token, userId, request);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                System.out.println("Ответ сервера: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    System.out.println("Обновленная роль: " + response.body().getRole());
                }
                handleResponse(response, onSuccess, onError, "Ошибка обновления роли");
            }

            @Override
            public void onFailure(Call<ProfileResponse> call, Throwable t) {
                System.out.println("Сетевая ошибка: " + t.getMessage());
                handleFailure(t, onError);
            }
        });
    }

    // Удаление пользователя (асинхронно)
    public void deleteUserAsync(Long userId, Consumer<Void> onSuccess, Consumer<String> onError) {
        if (!checkAuthentication(onError)) return;

        String token = getAuthHeader();
        Call<Void> call = adminUserRepository.deleteUser(token, userId);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                handleResponse(response, onSuccess, onError, "Ошибка удаления пользователя");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                handleFailure(t, onError);
            }
        });
    }

    // Получение уникальных ролей (без изменений)
    public Set<String> getUniqueRoles(List<ProfileResponse> users) {
        return users.stream()
                .map(ProfileResponse::getRole)
                .filter(role -> role != null && !role.isBlank())
                .collect(Collectors.toSet());
    }

    // Фильтрация пользователей (без изменений)
    public List<ProfileResponse> filterUsers(List<ProfileResponse> allUsers,
                                             String searchText,
                                             String selectedRole) {
        String lowerSearchText = searchText.toLowerCase();
        return allUsers.stream()
                .filter(user -> matchesSearch(user, lowerSearchText))
                .filter(user -> matchesRole(user, selectedRole))
                .collect(Collectors.toList());
    }

    // Вспомогательные методы (исправленные)
    private boolean matchesSearch(ProfileResponse user, String searchText) {
        return searchText.isEmpty() ||
                (user.getUsername() != null && user.getUsername().toLowerCase().contains(searchText)) ||
                (user.getFirstName() != null && user.getFirstName().toLowerCase().contains(searchText)) ||
                (user.getLastName() != null && user.getLastName().toLowerCase().contains(searchText)) ||
                (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchText)) ||
                (user.getPhoneNumber() != null && user.getPhoneNumber().contains(searchText));
    }

    private boolean matchesRole(ProfileResponse user, String selectedRole) {
        if (selectedRole == null || selectedRole.equals("Все")) {
            return true;
        }
        return user.getRole() != null && user.getRole().equalsIgnoreCase(selectedRole);
    }

    // Проверка аутентификации
    private boolean checkAuthentication(Consumer<String> onError) {
        try {
            if (!SessionManager.isAuthenticated()) {
                Platform.runLater(() -> onError.accept("Ошибка: требуется аутентификация"));
                return false;
            }
            return true;
        } catch (Exception e) {
            Platform.runLater(() -> onError.accept("Ошибка сессии: " + e.getMessage()));
            return false;
        }
    }

    // Получение заголовка Authorization
    private String getAuthHeader() {
        return "Bearer " + SessionManager.getToken();
    }

    private <T> void handleResponse(Response<T> response,
                                    Consumer<T> onSuccess,
                                    Consumer<String> onError,
                                    String errorPrefix) {
        Platform.runLater(() -> {
            if (response.isSuccessful()) {
                onSuccess.accept(response.body());
            } else {
                try {
                    ErrorResponse error = ApiConfig.getErrorConverter()
                            .convert(response.errorBody());
                    String message = error.getMessage();
                    onError.accept(message);
                } catch (Exception e) {
                    System.err.println("Ошибка парсинга ответа: " + e.getMessage());
                    onError.accept("Ошибка " + response.code() + ": Не удалось обработать ответ сервера");
                }
            }
        });
    }

    // Обработчик сетевых ошибок
    private void handleFailure(Throwable t, Consumer<String> onError) {
        Platform.runLater(() -> {
            String message = "Сетевая ошибка: " + t.getMessage();
            onError.accept(message);
            t.printStackTrace();
        });
    }
}