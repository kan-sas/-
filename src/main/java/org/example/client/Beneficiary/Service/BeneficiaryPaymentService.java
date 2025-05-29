package org.example.client.Beneficiary.Service;

import okhttp3.ResponseBody;
import org.example.client.Beneficiary.Repository.BeneficiaryPaymentRepository;
import org.example.client.Beneficiary.dto.Request.PaymentRequest;
import org.example.client.Beneficiary.dto.Response.PaymentResponse;
import org.example.client.Beneficiary.exception.BeneficiaryServiceException;
import org.example.client.core.common.dto.ErrorResponse;
import org.example.client.core.common.util.SessionManager;
import org.example.client.core.config.ApiConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class BeneficiaryPaymentService {
    private static final Logger logger = LoggerFactory.getLogger(BeneficiaryPaymentService.class);
    private final BeneficiaryPaymentRepository beneficiaryPaymentRepository;
    private final Converter<ResponseBody, ErrorResponse> errorConverter;

    public BeneficiaryPaymentService() {
        this.beneficiaryPaymentRepository = ApiConfig.getRetrofit().create(BeneficiaryPaymentRepository.class);
        this.errorConverter = ApiConfig.getErrorConverter();
        logger.debug("Инициализирован сервис выплат с репозиторием: {}", beneficiaryPaymentRepository.getClass().getSimpleName());
    }

    // Основные CRUD операции

    public PaymentResponse getPaymentById(Long id) throws BeneficiaryServiceException {
        logger.debug("Запрос выплаты по ID: {}", id);
        return executeCall(
                beneficiaryPaymentRepository.getPaymentById(id),
                "получения выплаты с ID: " + id
        );
    }

    public List<PaymentResponse> getAllPaymentsByBeneficiary(Long beneficiaryId) throws BeneficiaryServiceException {
        logger.debug("Запрос всех выплат для бенефициара ID: {}", beneficiaryId);
        return executeCall(
                beneficiaryPaymentRepository.getAllPaymentsByBeneficiary(beneficiaryId),
                "получения всех выплат для получателя: " + beneficiaryId
        );
    }

    public List<PaymentResponse> getPendingPayments() throws BeneficiaryServiceException {
        logger.debug("Запрос ожидающих выплат");
        return executeCall(
                beneficiaryPaymentRepository.getPendingPayments(),
                "получения ожидающих выплат"
        );
    }

    public PaymentResponse createPayment(PaymentRequest request) throws BeneficiaryServiceException {
        logger.info("Создание новой выплаты: {}", request);
        Long userId = SessionManager.getUserId();

        if (userId == null) {
            throw new BeneficiaryServiceException("Пользователь не аутентифицирован");
        }

        validatePaymentRequest(request);

        PaymentRequest enriched = PaymentRequest.builder()
                .beneficiaryId(userId)
                .amount(request.getAmount())
                .type(request.getType())
                .department(request.getDepartment())
                .comment(request.getComment())
                .build();

        PaymentResponse response = executeCall(
                beneficiaryPaymentRepository.createPayment(enriched),
                "создания выплаты"
        );

        logger.info("Выплата успешно создана: ID {}", response.getId());
        return response;
    }

    public PaymentResponse updatePayment(Long id, PaymentRequest request) throws BeneficiaryServiceException {
        logger.debug("🔼 PUT /payments/{}: {}", id, request);
        validateUpdateRequest(request);

        try {
            Response<PaymentResponse> response = beneficiaryPaymentRepository
                    .updatePayment(id, request)
                    .execute();

            if (!response.isSuccessful()) {
                logger.error("Ошибка обновления: {}", response.errorBody().string());
            }
            return handleResponse(response, "обновления выплаты");
        } catch (IOException e) {
            logger.error("Сетевой сбой: {}", e.getMessage());
            throw new BeneficiaryServiceException("Ошибка сети");
        }
    }

    public void deletePayment(Long id) throws BeneficiaryServiceException {
        logger.warn("Удаление выплаты ID: {}", id);
        executeVoidCall(
                beneficiaryPaymentRepository.deletePayment(id),
                "удаления выплаты с ID: " + id
        );
        logger.info("Выплата ID {} успешно удалена", id);
    }

    // Валидация запросов
    private void validatePaymentRequest(PaymentRequest request) throws BeneficiaryServiceException {
        logger.trace("Валидация запроса на выплату: {}", request);

        if (request.getAmount() == null || !isValidAmount(request.getAmount())) {
            logger.warn("Невалидная сумма выплаты: {}", request.getAmount());
            throw new BeneficiaryServiceException("Некорректная сумма выплаты");
        }

        if (request.getType() == null || request.getType().isBlank()) {
            logger.warn("Отсутствует тип выплаты в запросе: {}", request);
            throw new BeneficiaryServiceException("Тип выплаты обязателен");
        }
    }

    private void validateUpdateRequest(PaymentRequest request) throws BeneficiaryServiceException {
        if (request.allFieldsNull()) {
            throw new BeneficiaryServiceException("Не указаны поля для обновления");
        }

        if (request.getAmount() != null && !isValidAmount(request.getAmount())) {
            throw new BeneficiaryServiceException("Некорректный формат суммы");
        }
    }

    private boolean isValidAmount(String amount) {
        try {
            BigDecimal value = new BigDecimal(amount);
            boolean isValid = value.compareTo(BigDecimal.ZERO) > 0;
            if (!isValid) {
                logger.warn("Сумма должна быть больше нуля: {}", amount);
            }
            return isValid;
        } catch (NumberFormatException e) {
            logger.warn("Некорректный формат суммы: {}", amount, e);
            return false;
        }
    }


    private <T> T executeCall(Call<T> call, String operationName) throws BeneficiaryServiceException {
        try {
            logger.debug("Выполнение {} запроса: {}", operationName, call.request().url());
            Response<T> response = call.execute();
            logger.trace("Получен ответ для {}: код {}", operationName, response.code());
            return handleResponse(response, operationName);
        } catch (IOException e) {
            String errorMsg = "Ошибка при " + operationName;
            logger.error("{} [URL: {}]", errorMsg, call.request().url(), e);
            throw new BeneficiaryServiceException(errorMsg, e);
        }
    }

    private void executeVoidCall(Call<Void> call, String operationName) throws BeneficiaryServiceException {
        try {
            Response<Void> response = call.execute();
            handleVoidResponse(response, operationName);
        } catch (IOException e) {
            String errorMsg = "Ошибка при " + operationName;
            logger.error(errorMsg, e);
            throw new BeneficiaryServiceException(errorMsg, e);
        }
    }

    private <T> T handleResponse(Response<T> response, String operationName) throws BeneficiaryServiceException {
        if (response.isSuccessful()) {
            logger.debug("Успешный ответ {}: {}", operationName, response.body());
            return response.body();
        } else {
            ErrorResponse error = parseError(response);
            String errorBody = getErrorBody(response);
            String message = String.format("Ошибка %s (%d): %s",
                    operationName,
                    response.code(),
                    error != null ? error.getMessage() : "Неизвестная ошибка"
            );
            logger.error("{}\nТело ошибки: {}", message, errorBody);
            throw new BeneficiaryServiceException(message);
        }
    }

    private void handleVoidResponse(Response<Void> response, String operationName) throws BeneficiaryServiceException {
        if (!response.isSuccessful()) {
            ErrorResponse error = parseError(response);
            String message = String.format("Ошибка %s (%d): %s",
                    operationName,
                    response.code(),
                    error != null ? error.getMessage() : "Неизвестная ошибка"
            );
            logger.error(message);
            throw new BeneficiaryServiceException(message);
        }
    }
    private String getErrorBody(Response<?> response) {
        try {
            return response.errorBody() != null ? response.errorBody().string() : "empty body";
        } catch (IOException e) {
            logger.warn("Ошибка чтения тела ошибки", e);
            return "unreadable body";
        }
    }

    private ErrorResponse parseError(Response<?> response) {
        try {
            ErrorResponse error = errorConverter.convert(Objects.requireNonNull(response.errorBody()));
            logger.trace("Распарсена ошибка: {}", error);
            return error;
        } catch (IOException e) {
            logger.error("Ошибка парсинга ошибки. Тело ответа: {}", getErrorBody(response), e);
            return null;
        }
    }
}