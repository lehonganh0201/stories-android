package com.example.stories_project;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.stories_project.databinding.ActivityMainBinding;
import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.model.Story;
import com.example.stories_project.network.RetrofitClient;
import com.example.stories_project.ui.StoryAdapter;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private StoryAdapter storyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storyAdapter = new StoryAdapter();
        binding.storyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.storyRecyclerView.setAdapter(storyAdapter);

        storyAdapter.setOnItemClickListener(new StoryAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Story story) {
                Toast.makeText(MainActivity.this, "Clicked: " + story.getName(), Toast.LENGTH_SHORT).show();
            }
        });

        fetchStories();
    }

    private void fetchStories() {
        binding.progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getStoryApiService().getStories().enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if ("SUCCESS".equals(apiResponse.getMeta().getStatus())) {
                        storyAdapter.submitList(apiResponse.getData() != null ? apiResponse.getData() : new ArrayList<>());
                    } else {
                        Toast.makeText(MainActivity.this, "API Error: Invalid status", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}