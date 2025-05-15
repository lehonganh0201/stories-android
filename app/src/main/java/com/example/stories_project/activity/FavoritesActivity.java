package com.example.stories_project.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.stories_project.MainActivity;
import com.example.stories_project.R;
import com.example.stories_project.databinding.ActivityFavoritesBinding;
import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.network.RetrofitClient;
import com.example.stories_project.network.response.UserStoryFavoriteResponse;
import com.example.stories_project.ui.FavoriteAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoritesActivity extends AppCompatActivity {

    private ActivityFavoritesBinding binding;
    private FavoriteAdapter favoriteAdapter;
    private PopupWindow popupWindow;
    private boolean isMenuShowing = false;
    private static final String PREF_NAME = "UserPrefs";
    private View overlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityFavoritesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        favoriteAdapter = new FavoriteAdapter(favorite -> {
            Intent intent = new Intent(FavoritesActivity.this, StoryDetailActivity.class);
            intent.putExtra("slugName", favorite.storySlug());
            startActivity(intent);
        });

        binding.favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.favoritesRecyclerView.setAdapter(favoriteAdapter);

        binding.searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(FavoritesActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        binding.bellButton.setOnClickListener(v -> {
            if (isMenuShowing) {
                dismissMenu();
            } else {
                showProfileMenu();
            }
        });

        fetchFavorites();
    }

    private void fetchFavorites() {
        binding.progressBar.setVisibility(View.VISIBLE);
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String username = prefs.getString("username", null);

        if (username == null) {
            Toast.makeText(this, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            binding.progressBar.setVisibility(View.GONE);
            return;
        }

        RetrofitClient.getStoryApiService().getUserFavorites(username).enqueue(new Callback<ApiResponse<List<UserStoryFavoriteResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserStoryFavoriteResponse>>> call, Response<ApiResponse<List<UserStoryFavoriteResponse>>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<UserStoryFavoriteResponse>> apiResponse = response.body();
                    if ("SUCCESS".equals(apiResponse.getMeta().getStatus())) {
                        List<UserStoryFavoriteResponse> favorites = apiResponse.getData() != null ? apiResponse.getData() : new ArrayList<>();
                        favoriteAdapter.submitList(favorites);
                        binding.favoritesRecyclerView.setVisibility(favorites.isEmpty() ? View.GONE : View.VISIBLE);
                        if (favorites.isEmpty()) {
                            Toast.makeText(FavoritesActivity.this, "Danh sách yêu thích trống", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(FavoritesActivity.this, "Lỗi API: Trạng thái không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(FavoritesActivity.this, "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UserStoryFavoriteResponse>>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(FavoritesActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProfileMenu() {
        View menuView = LayoutInflater.from(this).inflate(R.layout.menu_profile, null);

        popupWindow = new PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        popupWindow.setElevation(8f);

        overlayView = new View(this);
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(0x20000000); // Very light semi-transparent black (~13% opacity)
        overlayView.setClickable(true);

        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        decorView.addView(overlayView);

        overlayView.setOnClickListener(v -> dismissMenu());

        menuView.findViewById(R.id.menu_account_info).setOnClickListener(v -> {
            // TODO: Navigate to AccountInfoActivity
             Intent intent = new Intent(FavoritesActivity.this, AccountInfoActivity.class);
             startActivity(intent);
            dismissMenu();
        });

        menuView.findViewById(R.id.menu_favorites).setOnClickListener(v -> {
            Toast.makeText(this, "Danh sách yêu thích clicked", Toast.LENGTH_SHORT).show();
            dismissMenu();
        });

        menuView.findViewById(R.id.menu_reading_history).setOnClickListener(v -> {
            // TODO: Navigate to ReadingHistoryActivity
             Intent intent = new Intent(FavoritesActivity.this, ReadingHistoryActivity.class);
             startActivity(intent);
            dismissMenu();
        });

        menuView.findViewById(R.id.menu_logout).setOnClickListener(v -> {
            logout();
            dismissMenu();
        });

        int[] location = new int[2];
        binding.bellButton.getLocationOnScreen(location);
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.NO_GRAVITY, location[0], location[1] + binding.bellButton.getHeight());
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

        Intent intent = new Intent(FavoritesActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissMenu();
    }
}