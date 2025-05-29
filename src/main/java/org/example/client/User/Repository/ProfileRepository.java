package org.example.client.User.Repository;

import org.example.client.User.dto.Request.UpdateProfileRequest;
import org.example.client.core.common.dto.ProfileResponse;
import retrofit2.Call;
import retrofit2.http.*;

public interface ProfileRepository {

    @GET("user/profile")
    Call<ProfileResponse> getProfile();

    @DELETE("user/profile")
    Call<Void> deleteProfile();

    @PATCH("user/profile/password")
    Call<Void> updatePassword(@Body UpdateProfileRequest request);

    @PATCH("user/profile/contact")
    Call<Void> updateContactInfo(@Body UpdateProfileRequest request);

}
