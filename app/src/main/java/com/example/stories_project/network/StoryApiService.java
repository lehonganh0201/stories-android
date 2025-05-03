package com.example.stories_project.network;

import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.model.Category;
import com.example.stories_project.model.Story;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
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
}
