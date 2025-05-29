package org.example.client.Beneficiary.Repository;

import org.example.client.Beneficiary.dto.Request.PaymentRequest;
import org.example.client.Beneficiary.dto.Response.PaymentResponse;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface BeneficiaryPaymentRepository {

    // Создание выплаты
    @POST("payments")
    Call<PaymentResponse> createPayment(@Body PaymentRequest request);

    // Получение выплаты по ID (новый путь)
    @GET("payments/{id}")
    Call<PaymentResponse> getPaymentById(@Path("id") Long id);

    // Получение всех выплат бенефициара (включая подтвержденные)
    @GET("payments/beneficiary/{beneficiaryId}/all")
    Call<List<PaymentResponse>> getAllPaymentsByBeneficiary(
            @Path("beneficiaryId") Long beneficiaryId);

    // Обновление выплаты
    @PATCH("payments/{id}")
    Call<PaymentResponse> updatePayment(
            @Path("id") Long id,
            @Body PaymentRequest request);

    // Удаление выплаты
    @DELETE("payments/{id}")
    Call<Void> deletePayment(@Path("id") Long id);

    // Получение выплат по статусу (для социального работника)
    @GET("payments/pending")
    Call<List<PaymentResponse>> getPendingPayments();
}