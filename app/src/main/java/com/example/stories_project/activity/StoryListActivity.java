package com.example.stories_project.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stories_project.MainActivity;
import com.example.stories_project.R;
import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.model.Story;
import com.example.stories_project.network.RetrofitClient;
import com.example.stories_project.ui.StoryAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoryListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StoryAdapter adapter;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private TextView titleTextView;
    private int currentPage = 1;
    private String categorySlug;
    private List<Story> allStories = new ArrayList<>();
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private PopupWindow popupWindow;
    private boolean isMenuShowing = false;
    private static final String PREF_NAME = "UserPrefs";
    private View overlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_list);

        String categoryName = getIntent().getStringExtra("categoryName");
        categorySlug = getIntent().getStringExtra("categorySlug");

        titleTextView = findViewById(R.id.titleTextView);
        recyclerView = findViewById(R.id.storyRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        errorTextView = findViewById(R.id.errorTextView);
        ImageButton searchButton = findViewById(R.id.searchButton);
        ImageButton bellButton = findViewById(R.id.bellButton);

        if (categoryName != null) {
            titleTextView.setText("Thể loại: " + categoryName);
        }

        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new StoryAdapter();
        recyclerView.setAdapter(adapter);

        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
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

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 4) {
                    currentPage++;
                    fetchStoriesByGenre(categorySlug, currentPage, false);
                }
            }
        });

        searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(StoryListActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        bellButton.setOnClickListener(v -> {
            if (isMenuShowing) {
                dismissMenu();
            } else {
                showProfileMenu();
            }
        });

        if (categorySlug != null) {
            fetchStoriesByGenre(categorySlug, currentPage, true);
        } else {
            showError("Không tìm thấy slug danh mục");
        }
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
            // TODO: Navigate to AccountInfoActivity
             Intent intent = new Intent(StoryListActivity.this, AccountInfoActivity.class);
             startActivity(intent);
            dismissMenu();
        });

        menuView.findViewById(R.id.menu_favorites).setOnClickListener(v -> {
            // TODO: Navigate to FavoritesActivity
             Intent intent = new Intent(StoryListActivity.this, FavoritesActivity.class);
             startActivity(intent);
            dismissMenu();
        });

        menuView.findViewById(R.id.menu_reading_history).setOnClickListener(v -> {
            // TODO: Navigate to ReadingHistoryActivity
             Intent intent = new Intent(StoryListActivity.this, ReadingHistoryActivity.class);
             startActivity(intent);
            dismissMenu();
        });

        menuView.findViewById(R.id.menu_logout).setOnClickListener(v -> {
            logout();
            dismissMenu();
        });

        ImageButton bellButton = findViewById(R.id.bellButton);
        int[] location = new int[2];
        bellButton.getLocationOnScreen(location);
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.NO_GRAVITY, location[0], location[1] + bellButton.getHeight());
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

        Intent intent = new Intent(StoryListActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }

    private void fetchStoriesByGenre(String slug, int pageNumber, boolean isFirstLoad) {
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);
        if (isFirstLoad) {
            recyclerView.setVisibility(View.GONE);
        }

        Call<ApiResponse<List<Story>>> call = RetrofitClient.getStoryApiService().getStoriesByGenre(slug, pageNumber);
        call.enqueue(new Callback<ApiResponse<List<Story>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Story>>> call, Response<ApiResponse<List<Story>>> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Story>> apiResponse = response.body();
                    if ("SUCCESS".equals(apiResponse.getMeta().getStatus())) {
                        List<Story> stories = apiResponse.getData();
                        if (stories.isEmpty()) {
                            if (isFirstLoad) {
                                showError("Không có truyện nào trong danh mục này");
                            }
                            isLastPage = true;
                        } else {
                            if (isFirstLoad) {
                                allStories.clear();
                            }
                            allStories.addAll(stories);
                            adapter.submitList(new ArrayList<>(allStories)); // Tạo bản sao để tránh lỗi ConcurrentModification
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        showError("Lỗi API: " + apiResponse.getMeta().getStatus());
                    }
                } else {
                    showError("Không thể lấy danh sách truyện");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Story>>> call, Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                showError("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        errorTextView.setText(message);
        errorTextView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissMenu();
    }
}