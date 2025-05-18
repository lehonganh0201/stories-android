package com.example.stories_project.network;

import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.model.Category;
import com.example.stories_project.model.ChapterReader;
import com.example.stories_project.model.Story;
import com.example.stories_project.network.request.ChapterRequest;
import com.example.stories_project.network.request.ForgotPasswordRequest;
import com.example.stories_project.network.request.LoginRequest;
import com.example.stories_project.network.request.RegisterRequest;
import com.example.stories_project.network.request.ResetPasswordRequest;
import com.example.stories_project.network.request.UpdateUserRequest;
import com.example.stories_project.network.request.UserFavoriteRequest;
import com.example.stories_project.network.request.UserHistoryRequest;
import com.example.stories_project.network.request.VerifyOtpRequest;
import com.example.stories_project.network.response.AccountResponse;
import com.example.stories_project.network.response.ChapterResponse;
import com.example.stories_project.network.response.UserResponse;
import com.example.stories_project.network.response.UserStoryFavoriteResponse;
import com.example.stories_project.network.response.UserStoryHistoryResponse;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface StoryApiService {
    @GET("books/category/{category}")
    Call<ApiResponse<List<Story>>> getStories(
            @Path("category") String category,
            @Query("pageNumber") int pageNumber
    );

    @GET("genres")
    Call<ApiResponse<List<Category>>> getCategories();

    @GET("books/name/{slugName}")
    Call<ApiResponse<Story>> getStory(@Path("slugName") String slugName);

    @GET("books/genres/{slugGenres}")
    Call<ApiResponse<List<Story>>> getStoriesByGenre(
            @Path("slugGenres") String slug,
            @Query("pageNumber") int pageNumber
    );

    @POST("chapters")
    Call<ApiResponse<ChapterReader>> getChapterDetail(@Body ChapterRequest request);

    @GET("chapters/{slugName}")
    Call<ApiResponse<List<ChapterResponse>>> getAllChapters(@Path("slugName") String slugName);

    @GET("books/search")
    Call<ApiResponse<List<Story>>> searchStoryByKeyword(
            @Query("keywords") String keywords
    );

    @POST("auths/register")
    Call<ApiResponse<AccountResponse>> register(@Body RegisterRequest request);

    @POST("auths/login")
    Call<ApiResponse<AccountResponse>> login(@Body LoginRequest request);

    @POST("auths/verify-otp")
    Call<ApiResponse<AccountResponse>> verifyOtp(@Body VerifyOtpRequest request);

    @POST("auths/forgot-password")
    Call<ApiResponse<AccountResponse>> forgotPassword(@Body ForgotPasswordRequest request);

    @POST("auths/reset-password")
    Call<ApiResponse<AccountResponse>> resetPassword(@Body ResetPasswordRequest request);

    @POST("favorites")
    Call<ApiResponse<UserStoryFavoriteResponse>> saveFavorite(@Body UserFavoriteRequest request);

    @POST("favorites/remove")
    Call<ApiResponse<Void>> removeFavorite(@Body UserFavoriteRequest request);

    @GET("favorites/{username}")
    Call<ApiResponse<List<UserStoryFavoriteResponse>>> getUserFavorites(@Path("username") String username);

    @POST("favorites/check")
    Call<ApiResponse<Boolean>> isStoryFavorited(@Body UserFavoriteRequest request);

    @POST("histories")
    Call<ApiResponse<UserStoryHistoryResponse>> saveUserHistory(@Body UserHistoryRequest request);

    @GET("histories/{username}")
    Call<ApiResponse<List<UserStoryHistoryResponse>>> getUserHistory(@Path("username") String username);

    @POST("histories/last-read")
    Call<ApiResponse<UserStoryHistoryResponse>> getLastReadChapter(@Body UserHistoryRequest request);

    @GET("users/{username}")
    Call<ApiResponse<UserResponse>> getUserByUsername(@Path("username") String username);

    @PUT("users/{username}")
    Call<ApiResponse<UserResponse>> updateUserInfo(@Path("username") String username, @Body UpdateUserRequest request);

    @Multipart
    @PUT("users/upload")
    Call<ApiResponse<UserResponse>> uploadAvatar(
            @Part("username") String username,
            @Part MultipartBody.Part file
    );
}
