package org.example.client.core.config;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import org.example.client.core.common.dto.ErrorResponse;
import org.example.client.core.common.util.JwtUtil;
import org.example.client.core.common.util.SessionManager;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ApiConfig {
    private static final String BASE_URL = "http://localhost:8080/";
    private static Retrofit retrofit;

    public static String getBaseUrl() {
        return BASE_URL;
    }

    public static synchronized Retrofit getRetrofit() {
        if (retrofit == null) {
            System.out.println("[ApiConfig] Старт инициализации Retrofit");

            // 1) Логируем JSON-трафик тела запросов/ответов
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(message -> {
                System.out.println("[API_JSON] " + message);
            });
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            System.out.println("[ApiConfig] Добавлен HttpLoggingInterceptor с уровнем BODY");

            // 2) Создаём Gson с поддержкой java.time и красивым выводом
            Gson gson = Converters.registerAll(
                    new GsonBuilder()
                            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                            .setPrettyPrinting()
            ).create();
            System.out.println("[ApiConfig] Gson создан с поддержкой JavaTime и pretty printing");

            // 3) Interceptor для добавления и проверки JWT
            Interceptor authInterceptor = chain -> {
                Request original = chain.request();
                System.out.println("[ApiConfig] Interceptor: входящий запрос " +
                        original.method() + " " + original.url());

                String token = null;
                boolean isTokenValid = false;
                try {
                    token = SessionManager.getToken();
                    if (token != null) {
                        isTokenValid = JwtUtil.isTokenValid(token);
                        System.out.println("[ApiConfig] Найден токен, валидность=" + isTokenValid);
                        if (!isTokenValid) {
                            System.out.println("[ApiConfig] Токен невалиден, сброс сессии");
                            SessionManager.clearSession();
                            token = null;
                        }
                    } else {
                        System.out.println("[ApiConfig] Токен отсутствует");
                    }
                } catch (Exception e) {
                    System.out.println("[ApiConfig] Ошибка при проверке токена: " + e.getMessage());
                    token = null;
                }

                Request.Builder builder = original.newBuilder();
                if (token != null && !token.isEmpty()) {
                    builder.header("Authorization", "Bearer " + token);
                    System.out.println("[ApiConfig] В заголовок добавлен Authorization: Bearer <token>");
                }
                Request requestWithAuth = builder.build();

                long startNs = System.nanoTime();
                Response response = chain.proceed(requestWithAuth);
                long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

                System.out.println("[ApiConfig] Ответ получен за " + tookMs +
                        " ms, код=" + response.code() +
                        ", URL=" + response.request().url());

                return response;
            };
            System.out.println("[ApiConfig] Добавлен custom AuthInterceptor");

            // 4) Собираем OkHttpClient
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)     // лог тела
                    .addInterceptor(authInterceptor)        // JWT + метрики
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
            System.out.println("[ApiConfig] OkHttpClient собран с интерсепторами");

            // 5) Собираем Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            System.out.println("[ApiConfig] Retrofit инициализирован на " + BASE_URL);
        }
        return retrofit;
    }

    public static Converter<ResponseBody, ErrorResponse> getErrorConverter() {
        System.out.println("[ApiConfig] Создаём ErrorConverter для ErrorResponse");
        return getRetrofit().responseBodyConverter(
                ErrorResponse.class,
                new java.lang.annotation.Annotation[0]
        );
    }
}
