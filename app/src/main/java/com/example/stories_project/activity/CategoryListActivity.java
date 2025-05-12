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

import androidx.appcompat.app.AppCompatActivity;

import com.example.stories_project.MainActivity;
import com.example.stories_project.R;
import com.example.stories_project.databinding.ActivityCategoryListBinding;
import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.model.Category;
import com.example.stories_project.network.RetrofitClient;
import com.example.stories_project.ui.CategoryListAdapter;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryListActivity extends AppCompatActivity {
    private ActivityCategoryListBinding binding;
    private CategoryListAdapter categoryAdapter;
    private PopupWindow popupWindow;
    private boolean isMenuShowing = false;
    private static final String PREF_NAME = "UserPrefs";
    private View overlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        categoryAdapter = new CategoryListAdapter(category -> {
            Toast.makeText(this, "Đã chọn thể loại: " + category.getName(), Toast.LENGTH_SHORT).show();
            // TODO: Navigate to category-specific activity if needed
            // Intent intent = new Intent(CategoryListActivity.this, StoryListActivity.class);
            // intent.putExtra("categorySlug", category.getSlug());
            // startActivity(intent);
        });

        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        binding.categoryRecyclerView.setLayoutManager(layoutManager);
        binding.categoryRecyclerView.setAdapter(categoryAdapter);

        binding.searchButton.setOnClickListener(v -> {
            if (isMenuShowing) {
                dismissMenu();
            } else {
                showProfileMenu();
            }
        });

        fetchCategories();
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
            // Intent intent = new Intent(CategoryListActivity.this, AccountInfoActivity.class);
            // startActivity(intent);
            dismissMenu();
        });

        menuView.findViewById(R.id.menu_favorites).setOnClickListener(v -> {
            // TODO: Navigate to FavoritesActivity
             Intent intent = new Intent(CategoryListActivity.this, FavoritesActivity.class);
             startActivity(intent);
            dismissMenu();
        });

        menuView.findViewById(R.id.menu_reading_history).setOnClickListener(v -> {
            Toast.makeText(this, "Lịch sử đọc truyện clicked", Toast.LENGTH_SHORT).show();
            // TODO: Navigate to ReadingHistoryActivity
             Intent intent = new Intent(CategoryListActivity.this, ReadingHistoryActivity.class);
             startActivity(intent);
            dismissMenu();
        });

        menuView.findViewById(R.id.menu_logout).setOnClickListener(v -> {
            logout();
            dismissMenu();
        });

        int[] location = new int[2];
        binding.searchButton.getLocationOnScreen(location);
        popupWindow.showAtLocation(binding.getRoot(), Gravity.NO_GRAVITY, location[0], location[1] + binding.searchButton.getHeight());
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

        Intent intent = new Intent(CategoryListActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(CategoryListActivity.this, "Lỗi API: Trạng thái không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CategoryListActivity.this, "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Category>>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(CategoryListActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissMenu();
    }
}