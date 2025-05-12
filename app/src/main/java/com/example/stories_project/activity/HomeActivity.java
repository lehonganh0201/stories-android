package com.example.stories_project.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.stories_project.MainActivity;
import com.example.stories_project.R;
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
    private PopupWindow popupWindow;
    private boolean isMenuShowing = false;
    private static final String PREF_NAME = "UserPrefs";
    private View overlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        newBookAdapter = new StoryAdapter();
        comingSoonAdapter = new StoryAdapter();
        categoryAdapter = new CategoryAdapter(category -> {
            Intent intent = new Intent(HomeActivity.this, StoryDetailActivity.class);
            intent.putExtra("categoryName", category.getName());
            intent.putExtra("categorySlug", category.getSlug());
            startActivity(intent);
        });
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

        binding.searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        binding.bellButton.setOnClickListener(v -> {
            if (isMenuShowing) {
                dismissMenu();
            } else {
                showProfileMenu();
            }
        });

        binding.newBookShowMore.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, StatusStoryListActivity.class);
            intent.putExtra("tabIndex", 0); // Truyện mới
            startActivity(intent);
        });

        binding.comingSoonShowMore.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, StatusStoryListActivity.class);
            intent.putExtra("tabIndex", 1); // Sắp ra mắt
            startActivity(intent);
        });

        binding.categoryShowMore.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, CategoryListActivity.class);
            startActivity(intent);
        });

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

    private void showProfileMenu() {
        View menuView = LayoutInflater.from(this).inflate(R.layout.menu_profile, null);

        popupWindow = new PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        popupWindow.setElevation(8f);

        overlayView = new View(this);
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(0x20000000);
        overlayView.setClickable(true);

        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        decorView.addView(overlayView);

        overlayView.setOnClickListener(v -> dismissMenu());

        menuView.findViewById(R.id.menu_account_info).setOnClickListener(v -> {
            Toast.makeText(this, "Thông tin tài khoản clicked", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to AccountInfoActivity
            // Intent intent = new Intent(HomeActivity.this, AccountInfoActivity.class);
            // startActivity(intent);
            dismissMenu();
        });

        menuView.findViewById(R.id.menu_favorites).setOnClickListener(v -> {
            // TODO: Navigate to FavoritesActivity
             Intent intent = new Intent(HomeActivity.this, FavoritesActivity.class);
             startActivity(intent);
             dismissMenu();
        });

        menuView.findViewById(R.id.menu_reading_history).setOnClickListener(v -> {
            // TODO: Navigate to ReadingHistoryActivity
             Intent intent = new Intent(HomeActivity.this, ReadingHistoryActivity.class);
             startActivity(intent);
            dismissMenu();
        });

        menuView.findViewById(R.id.menu_logout).setOnClickListener(v -> {
            logout();
            dismissMenu();
        });

        int[] location = new int[2];
        binding.bellButton.getLocationOnScreen(location);
        popupWindow.showAtLocation(binding.getRoot(), Gravity.NO_GRAVITY, location[0], location[1] + binding.bellButton.getHeight());
        isMenuShowing = true;

        popupWindow.setOnDismissListener(() -> {
            if (overlayView.getParent() != null) {
                ((ViewGroup) overlayView.getParent()).removeView(overlayView);
            }
            isMenuShowing = false;
        });
    }

    private void dismissMenu() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
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
        dismissMenu();
    }
}