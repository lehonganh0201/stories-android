package com.example.stories_project.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.stories_project.databinding.ActivityHomeBinding;
import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.model.Category;
import com.example.stories_project.model.Story;
import com.example.stories_project.network.RetrofitClient;
import com.example.stories_project.ui.BannerAdapter;
import com.example.stories_project.ui.CategoryAdapter;
import com.example.stories_project.ui.StoryAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    private StoryAdapter newBookAdapter, comingSoonAdapter;
    private CategoryAdapter categoryAdapter;
    private BannerAdapter bannerAdapter;
    private Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;
    private static final long BANNER_INTERVAL = 4000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        newBookAdapter = new StoryAdapter();
        comingSoonAdapter = new StoryAdapter();
        categoryAdapter = new CategoryAdapter(category ->
                Toast.makeText(this, "Đã chọn thể loại: " + category.getName(), Toast.LENGTH_SHORT).show()
        );
        bannerAdapter = new BannerAdapter(story -> {
            Intent intent = new Intent(HomeActivity.this, StoryDetailActivity.class);
            intent.putExtra("slugName", story.getSlug());
            startActivity(intent);
        });

        binding.newBookRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.newBookRecyclerView.setAdapter(newBookAdapter);

        binding.comingSoonRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.comingSoonRecyclerView.setAdapter(comingSoonAdapter);

        binding.categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.categoryRecyclerView.setAdapter(categoryAdapter);

        binding.bannerViewPager.setAdapter(bannerAdapter);

        binding.newBookRecyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int recyclerViewWidth = v.getWidth();
            float density = getResources().getDisplayMetrics().density;
            int cardViewWidth = (int) (160 * density + 16 * density);
            int totalGridWidth = cardViewWidth * 2;
            int extraSpace = recyclerViewWidth - totalGridWidth - (int) (16 * density);
            if (extraSpace > 0) {
                int padding = extraSpace / 2;
                v.setPadding(padding, v.getPaddingTop(), padding, v.getPaddingBottom());
            }
        });

        binding.comingSoonRecyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int recyclerViewWidth = v.getWidth();
            float density = getResources().getDisplayMetrics().density;
            int cardViewWidth = (int) (160 * density + 16 * density);
            int totalGridWidth = cardViewWidth * 2;
            int extraSpace = recyclerViewWidth - totalGridWidth - (int) (16 * density);
            if (extraSpace > 0) {
                int padding = extraSpace / 2;
                v.setPadding(padding, v.getPaddingTop(), padding, v.getPaddingBottom());
            }
        });

        binding.searchButton.setOnClickListener(v ->
                Toast.makeText(this, "Tìm kiếm được nhấn", Toast.LENGTH_SHORT).show()
        );

        binding.bellButton.setOnClickListener(v ->
                Toast.makeText(this, "Thông báo được nhấn", Toast.LENGTH_SHORT).show()
        );

        binding.newBookShowMore.setOnClickListener(v ->
                Toast.makeText(this, "Xem thêm truyện mới", Toast.LENGTH_SHORT).show()
        );

        binding.comingSoonShowMore.setOnClickListener(v ->
                Toast.makeText(this, "Xem thêm sắp ra mắt", Toast.LENGTH_SHORT).show()
        );

        binding.categoryShowMore.setOnClickListener(v ->
                Toast.makeText(this, "Xem thêm thể loại", Toast.LENGTH_SHORT).show()
        );

        bannerRunnable = () -> {
            int currentItem = binding.bannerViewPager.getCurrentItem();
            int itemCount = bannerAdapter.getItemCount();
            if (itemCount > 0) {
                binding.bannerViewPager.setCurrentItem((currentItem + 1) % itemCount, true);
            }
            bannerHandler.postDelayed(bannerRunnable, BANNER_INTERVAL);
        };
        bannerHandler.postDelayed(bannerRunnable, BANNER_INTERVAL);

        fetchNewBooks();
        fetchComingSoonBooks();
        fetchCategories();
        fetchBannerBooks();
    }

    private void fetchNewBooks() {
        binding.progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getStoryApiService().getStories("truyen-moi", 1).enqueue(new Callback<ApiResponse<List<Story>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Story>>> call, Response<ApiResponse<List<Story>>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Story>> apiResponse = response.body();
                    if ("SUCCESS".equals(apiResponse.getMeta().getStatus())) {
                        newBookAdapter.submitList(apiResponse.getData() != null ? apiResponse.getData() : new ArrayList<>());
                    } else {
                        Toast.makeText(HomeActivity.this, "API Error: Invalid status", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Story>>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchComingSoonBooks() {
        binding.progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getStoryApiService().getStories("sap-ra-mat", 1).enqueue(new Callback<ApiResponse<List<Story>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Story>>> call, Response<ApiResponse<List<Story>>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Story>> apiResponse = response.body();
                    if ("SUCCESS".equals(apiResponse.getMeta().getStatus())) {
                        comingSoonAdapter.submitList(apiResponse.getData() != null ? apiResponse.getData() : new ArrayList<>());
                    } else {
                        Toast.makeText(HomeActivity.this, "API Error: Invalid status", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Error: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Story>>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCategories() {
        binding.progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getStoryApiService().getCategories().enqueue(new Callback<ApiResponse<List<Category>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Category>>> call, Response<ApiResponse<List<Category>>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Category>> apiResponse = response.body();
                    if ("SUCCESS".equals(apiResponse.getMeta().getStatus())) {
                        categoryAdapter.submitList(apiResponse.getData() != null ? apiResponse.getData() : new ArrayList<>());
                    } else {
                        Toast.makeText(HomeActivity.this, "Lỗi API: Trạng thái không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(HomeActivity.this, "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Category>>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchBannerBooks() {
        binding.progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getStoryApiService().getStories("hoan-thanh", 1).enqueue(new Callback<ApiResponse<List<Story>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Story>>> call, Response<ApiResponse<List<Story>>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Story>> apiResponse = response.body();
                    if ("SUCCESS".equals(apiResponse.getMeta().getStatus())) {
                        bannerAdapter.submitList(apiResponse.getData() != null ? apiResponse.getData() : new ArrayList<>());
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Story>>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Banner Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bannerHandler.removeCallbacks(bannerRunnable);
    }
}