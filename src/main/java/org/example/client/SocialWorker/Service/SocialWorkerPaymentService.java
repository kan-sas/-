package org.example.client.SocialWorker.Service;

import org.example.client.Beneficiary.dto.Response.PaymentResponse;
import org.example.client.SocialWorker.Repository.SocialWorkerRepository;
import org.example.client.SocialWorker.dto.Request.PaymentStatusUpdateRequest;
import org.example.client.core.common.util.SessionManager;
import org.example.client.core.config.ApiConfig;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;

public class SocialWorkerPaymentService {

    private final SocialWorkerRepository repository;

    /**
     * Конструктор сервиса SocialWorkerPaymentService.
     * Инициализирует репозиторий с помощью ApiConfig.
     */
    public SocialWorkerPaymentService() {
        this.repository = ApiConfig.getRetrofit().create(SocialWorkerRepository.class);
    }

    /**
     * Обновляет статус выплаты.
     *
     * @param paymentId идентификатор выплаты для обновления
     * @param request объект PaymentStatusUpdateRequest с новым статусом
     * @return обновлённый PaymentResponse
     * @throws IOException если произошла ошибка при выполнении HTTP-запроса
     * @throws SecurityException если сессия невалидна или токен просрочен
     */
    public PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatusUpdateRequest request) throws IOException {
        checkSession();
        Call<PaymentResponse> call = repository.updatePaymentStatus(paymentId, request);
        Response<PaymentResponse> response = call.execute();
        if (!response.isSuccessful()) {
            handleErrorResponse(response);
        }
        return response.body();
    }

    /**
     * Получает все выплаты со статусом 'PENDING'.
     *
     * @return список объектов PaymentResponse для выплат со статусом 'PENDING'
     * @throws IOException если произошла ошибка при выполнении HTTP-запроса
     * @throws SecurityException если сессия невалидна или токен просрочен
     */
    public List<PaymentResponse> getPendingPayments() throws IOException {
        checkSession();
        Call<List<PaymentResponse>> call = repository.getPendingPayments();
        Response<List<PaymentResponse>> response = call.execute();
        if (!response.isSuccessful()) {
            handleErrorResponse(response);
        }
        return response.body();
    }

    /**
     * Получает выплаты со статусом 'APPROVED' за указанный период.
     *
     * @param startDate начальная дата в формате ISO (например, "2025-05-01")
     * @param endDate конечная дата в формате ISO (например, "2025-05-31")
     * @return список объектов PaymentResponse для выплат со статусом 'APPROVED' в указанном диапазоне дат
     * @throws IOException если произошла ошибка при выполнении HTTP-запроса
     * @throws SecurityException если сессия невалидна или токен просрочен
     */
    public List<PaymentResponse> getApprovedPayments(String startDate, String endDate) throws IOException {
        checkSession();
        Call<List<PaymentResponse>> call = repository.getApprovedPayments(startDate, endDate);
        Response<List<PaymentResponse>> response = call.execute();
        if (!response.isSuccessful()) {
            handleErrorResponse(response);
        }
        return response.body();
    }

    private void checkSession() {
        if (!SessionManager.isAuthenticated()) {
            throw new SecurityException("Invalid or expired session");
        }
    }

    private void handleErrorResponse(Response<?> response) throws IOException {
        if (response.code() == 401) {
            SessionManager.clearSession();
            throw new SecurityException("Unauthorized: Token invalid or expired");
        }
        throw new IOException("Request failed: " + response.message());
    }
}