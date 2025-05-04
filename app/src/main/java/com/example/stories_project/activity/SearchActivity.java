package com.example.stories_project.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.stories_project.databinding.ActivitySearchBinding;
import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.model.Story;
import com.example.stories_project.network.RetrofitClient;
import com.example.stories_project.ui.StoryAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {
    private ActivitySearchBinding binding;
    private StoryAdapter storyAdapter;
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private static final long SEARCH_DELAY = 500;
    private static final String TAG = "SearchActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        storyAdapter = new StoryAdapter();
        storyAdapter.setOnItemClickListener(story -> {
            Intent intent = new Intent(SearchActivity.this, StoryDetailActivity.class);
            intent.putExtra("slugName", story.getSlug());
            startActivity(intent);
            Log.d(TAG, "Clicked story: " + story.getSlug());
        });

        binding.searchResultsRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.searchResultsRecyclerView.setAdapter(storyAdapter);

        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                searchRunnable = () -> searchStories(query);
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY);
            }
        });

        binding.searchEditText.requestFocus();
    }

    private void searchStories(String query) {
        if (query.isEmpty()) {
            storyAdapter.submitList(new ArrayList<>());
            binding.noResultsTextView.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.GONE);
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.noResultsTextView.setVisibility(View.GONE);

        Log.d(TAG, "Searching for: " + query);
        RetrofitClient.getStoryApiService().searchStoryByKeyword(query).enqueue(new Callback<ApiResponse<List<Story>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Story>>> call, Response<ApiResponse<List<Story>>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Story>> apiResponse = response.body();
                    if ("SUCCESS".equals(apiResponse.getMeta().getStatus())) {
                        List<Story> stories = apiResponse.getData() != null ? apiResponse.getData() : new ArrayList<>();
                        storyAdapter.submitList(stories);
                        binding.noResultsTextView.setVisibility(stories.isEmpty() ? View.VISIBLE : View.GONE);
                        Log.d(TAG, "Found " + stories.size() + " stories");
                    } else {
                        Toast.makeText(SearchActivity.this, "Lỗi API: Trạng thái không hợp lệ", Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "API status not SUCCESS: " + apiResponse.getMeta().getStatus());
                    }
                } else {
                    Toast.makeText(SearchActivity.this, "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "API response failed: " + (response.message() != null ? response.message() : "Unknown error"));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Story>>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(SearchActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Search failed: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
    }
}