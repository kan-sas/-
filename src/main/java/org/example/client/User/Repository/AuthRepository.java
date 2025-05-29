package org.example.client.User.Repository;

import org.example.client.User.dto.Request.LoginRequest;
import org.example.client.User.dto.Request.RegistrationRequest;
import org.example.client.User.dto.Response.AuthResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthRepository {
    @POST("/public/auth/register")
    Call<Void> register(@Body RegistrationRequest request);

    @POST("/public/auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

}