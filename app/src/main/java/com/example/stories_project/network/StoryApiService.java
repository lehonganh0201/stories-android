package com.example.stories_project.network;

import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.model.Category;
import com.example.stories_project.model.ChapterReader;
import com.example.stories_project.model.Story;
import com.example.stories_project.network.request.ChapterRequest;
import com.example.stories_project.network.response.ChapterResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
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
}
