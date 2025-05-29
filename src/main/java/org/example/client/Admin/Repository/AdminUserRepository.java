package org.example.client.Admin.Repository;

import org.example.client.Admin.dto.Request.ChangeRoleRequest;
import org.example.client.core.common.dto.ProfileResponse;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface AdminUserRepository {
    @GET("admin/users/getall")
    Call<List<ProfileResponse>> getAllUsers(@Header("Authorization") String token);

    @GET("admin/users/{userId}")
    Call<ProfileResponse> getUserById(
            @Header("Authorization") String token,
            @Path("userId") Long userId
    );

    @PATCH("admin/users/{userId}/role")
    Call<ProfileResponse> changeUserRole(
            @Header("Authorization") String token,
            @Path("userId") Long userId,
            @Body ChangeRoleRequest request
    );

    @DELETE("admin/users/{userId}")
    Call<Void> deleteUser(
            @Header("Authorization") String token,
            @Path("userId") Long userId
    );
}