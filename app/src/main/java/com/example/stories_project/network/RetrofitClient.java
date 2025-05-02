package com.example.stories_project.network;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "http://10.0.2.2:8080/api/v1/";
    private static volatile StoryApiService storyApiService; // Thread-safe singleton

    public static StoryApiService getStoryApiService() {
        if (storyApiService == null) {
            synchronized (RetrofitClient.class) {
                if (storyApiService == null) {
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY); // Log chi tiết request/response

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(logging)
                            .build();

                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(client)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();

                    storyApiService = retrofit.create(StoryApiService.class);
                }
            }
        }
        return storyApiService;
    }
}