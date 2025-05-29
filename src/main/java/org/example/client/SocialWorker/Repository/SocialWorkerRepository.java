package org.example.client.SocialWorker.Repository;

import org.example.client.Beneficiary.dto.Response.PaymentResponse;
import org.example.client.SocialWorker.dto.Request.PaymentStatusUpdateRequest;
import retrofit2.Call;
import retrofit2.http.*;

import java.time.LocalDate;
import java.util.List;

public interface SocialWorkerRepository {

    /**
     * Обновить статус выплаты (PUT /api/payments/{paymentId}/status)
     */
    @PUT("api/payments/{paymentId}/status")
    Call<PaymentResponse> updatePaymentStatus(
            @Path("paymentId") Long paymentId,
            @Body PaymentStatusUpdateRequest request
    );

    /**
     * Получить все выплаты со статусом PENDING (GET /api/payments/pending)
     */
    @GET("api/payments/pending")
    Call<List<PaymentResponse>> getPendingPayments();

    /**
     * Получить выплаты со статусом APPROVED за указанный период
     * (GET /api/payments/approved?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD)
     */
    @GET("api/payments/approved")
    Call<List<PaymentResponse>> getApprovedPayments(
            @Query("startDate") String startDate,  // формат ISO: "2025-05-01"
            @Query("endDate")   String endDate     // формат ISO: "2025-05-31"
    );
}
