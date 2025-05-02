package com.example.stories_project.network;

import com.example.stories_project.model.ApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface StoryApiService {
    @GET("books/category/sap-ra-mat?pageNumber=1")
    Call<ApiResponse> getStories();
}
