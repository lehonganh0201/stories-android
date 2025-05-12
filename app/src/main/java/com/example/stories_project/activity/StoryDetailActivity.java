package com.example.stories_project.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.stories_project.R;
import com.example.stories_project.databinding.ActivityStoryDetailBinding;
import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.model.Category;
import com.example.stories_project.model.Chapter;
import com.example.stories_project.model.Story;
import com.example.stories_project.network.RetrofitClient;
import com.example.stories_project.network.request.UserFavoriteRequest;
import com.example.stories_project.network.request.UserHistoryRequest;
import com.example.stories_project.network.response.ChapterResponse;
import com.example.stories_project.network.response.UserStoryFavoriteResponse;
import com.example.stories_project.network.response.UserStoryHistoryResponse;
import com.example.stories_project.ui.ChapterAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoryDetailActivity extends AppCompatActivity {
    private ActivityStoryDetailBinding binding;
    private ChapterAdapter chapterAdapter;
    private static final String IMAGE_BASE_URL = "https://img.otruyenapi.com/uploads/comics/";
    private List<String> chapterPaths = new ArrayList<>();
    private List<Chapter> chapters = new ArrayList<>();
    private boolean isFavorited = false;
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";
    private String slugName;
    private Integer lastReadChapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStoryDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        chapterAdapter = new ChapterAdapter(chapter -> {
            Intent intent = new Intent(StoryDetailActivity.this, ChapterReaderActivity.class);
            intent.putExtra("chapterData", chapter.getChapterApiData());
            intent.putExtra("slugName", slugName);
            intent.putStringArrayListExtra("chapterPaths", new ArrayList<>(chapterPaths));
            startActivity(intent);
        });
        binding.chapterRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.chapterRecyclerView.setAdapter(chapterAdapter);

        slugName = getIntent().getStringExtra("slugName");
        if (slugName == null) {
            Toast.makeText(this, "Không tìm thấy slugName", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String username = prefs.getString(KEY_USERNAME, "");
        if (username.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để sử dụng các tính năng!", Toast.LENGTH_SHORT).show();
            binding.readButton.setEnabled(false);
            binding.likeButton.setEnabled(false);
            return;
        }

        checkFavoriteStatus(username, slugName);
        checkReadingHistory(username, slugName);

        binding.readButton.setOnClickListener(v -> {
            if (!chapterPaths.isEmpty()) {
                int chapterIndex = (lastReadChapter != null) ? (chapterPaths.size() - lastReadChapter) : 0;
                if (chapterIndex >= 0 && chapterIndex < chapterPaths.size()) {
                    String chapterData = chapterPaths.get(chapterIndex);
                    saveReadingHistory(username, lastReadChapter != null ? lastReadChapter : 1);
                    Intent intent = new Intent(StoryDetailActivity.this, ChapterReaderActivity.class);
                    intent.putExtra("chapterData", chapterData);
                    intent.putExtra("slugName", slugName);
                    intent.putStringArrayListExtra("chapterPaths", new ArrayList<>(chapterPaths));
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Chương không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Chưa có chapter nào", Toast.LENGTH_SHORT).show();
            }
        });

        binding.likeButton.setOnClickListener(v -> {
            binding.likeButton.setEnabled(false);
            UserFavoriteRequest request = new UserFavoriteRequest(username, slugName);
            if (isFavorited) {
                RetrofitClient.getStoryApiService().removeFavorite(request).enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                        if (response.code() == 204) {
                            checkFavoriteStatus(username, slugName);
                            Toast.makeText(StoryDetailActivity.this, "Đã xóa khỏi danh sách yêu thích!", Toast.LENGTH_SHORT).show();
                        } else {
                            binding.likeButton.setEnabled(true);
                            Toast.makeText(StoryDetailActivity.this, "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        binding.likeButton.setEnabled(true);
                        Toast.makeText(StoryDetailActivity.this, "Lỗi khi xóa favorite: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                RetrofitClient.getStoryApiService().saveFavorite(request).enqueue(new Callback<ApiResponse<UserStoryFavoriteResponse>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<UserStoryFavoriteResponse>> call, Response<ApiResponse<UserStoryFavoriteResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<UserStoryFavoriteResponse> apiResponse = response.body();
                            if ("SUCCESS".equals(apiResponse.getMeta().getStatus())) {
                                checkFavoriteStatus(username, slugName);
                                Toast.makeText(StoryDetailActivity.this, "Đã thêm vào danh sách yêu thích!", Toast.LENGTH_SHORT).show();
                            } else {
                                binding.likeButton.setEnabled(true);
                                Toast.makeText(StoryDetailActivity.this, "Lỗi: " + apiResponse.getMeta().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            binding.likeButton.setEnabled(true);
                            Toast.makeText(StoryDetailActivity.this, "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<UserStoryFavoriteResponse>> call, Throwable t) {
                        binding.likeButton.setEnabled(true);
                        Toast.makeText(StoryDetailActivity.this, "Lỗi khi thêm favorite: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        binding.downloadButton.setOnClickListener(v -> {
            Toast.makeText(this, "Bắt đầu tải truyện!", Toast.LENGTH_SHORT).show();
        });

        fetchStoryDetails(slugName);
        fetchChapterPaths(slugName);
    }

    private void checkFavoriteStatus(String username, String slugName) {
        UserFavoriteRequest request = new UserFavoriteRequest(username, slugName);
        RetrofitClient.getStoryApiService().isStoryFavorited(request).enqueue(new Callback<ApiResponse<Boolean>>() {
            @Override
            public void onResponse(Call<ApiResponse<Boolean>> call, Response<ApiResponse<Boolean>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Boolean> apiResponse = response.body();
                    if ("SUCCESS".equals(apiResponse.getMeta().getStatus()) && apiResponse.getData() != null) {
                        isFavorited = apiResponse.getData();
                        int iconRes = isFavorited ? R.drawable.ic_favorite_active : R.drawable.ic_favorite_default;
                        binding.likeButton.setIconTintResource(isFavorited ? R.color.favorite_active_tint : R.color.favorite_default_tint);
                        binding.likeButton.setIcon(ContextCompat.getDrawable(StoryDetailActivity.this, iconRes));
                        binding.likeButton.setEnabled(true);
                        Log.d("StoryDetailActivity", "Favorite status: " + isFavorited + ", Icon set: " + iconRes);
                    } else {
                        Toast.makeText(StoryDetailActivity.this, "Lỗi khi kiểm tra trạng thái yêu thích: " + apiResponse.getMeta().getMessage(), Toast.LENGTH_SHORT).show();
                        binding.likeButton.setEnabled(true);
                    }
                } else {
                    Toast.makeText(StoryDetailActivity.this, "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                    binding.likeButton.setEnabled(true);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Boolean>> call, Throwable t) {
                Toast.makeText(StoryDetailActivity.this, "Lỗi khi kiểm tra trạng thái: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                binding.likeButton.setEnabled(true);
            }
        });
    }

    private void checkReadingHistory(String username, String slugName) {
        binding.progressBar.setVisibility(View.VISIBLE);
        UserHistoryRequest request = new UserHistoryRequest(username, slugName, null);
        RetrofitClient.getStoryApiService().getLastReadChapter(request).enqueue(new Callback<ApiResponse<UserStoryHistoryResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserStoryHistoryResponse>> call, Response<ApiResponse<UserStoryHistoryResponse>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && "SUCCESS".equals(response.body().getMeta().getStatus())) {
                    UserStoryHistoryResponse history = response.body().getData();
                    if (history != null && history.storySlug().equals(slugName)) {
                        lastReadChapter = history.lastChapter();
                        updateReadButton();
                    } else {
                        lastReadChapter = null;
                        updateReadButton();
                    }
                } else {
                    lastReadChapter = null;
                    updateReadButton();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserStoryHistoryResponse>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Log.e("StoryDetailActivity", "Failed to check reading history: " + t.getMessage());
                lastReadChapter = null;
                updateReadButton();
            }
        });
    }

    private void updateReadButton() {
        if (chapterPaths.isEmpty()) {
            binding.readButton.setText("Đọc truyện");
            binding.readButton.setEnabled(false);
            return;
        }

        binding.readButton.setEnabled(true);
        if (lastReadChapter != null) {
            binding.readButton.setText("Đọc tiếp chương " + lastReadChapter);
        } else {
            binding.readButton.setText("Đọc truyện");
        }
    }

    private void saveReadingHistory(String username, int chapterNumber) {
        UserHistoryRequest request = new UserHistoryRequest(username, slugName, chapterNumber);
        RetrofitClient.getStoryApiService().saveUserHistory(request).enqueue(new Callback<ApiResponse<UserStoryHistoryResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserStoryHistoryResponse>> call, Response<ApiResponse<UserStoryHistoryResponse>> response) {
                if (!response.isSuccessful() || response.body() == null || !"SUCCESS".equals(response.body().getMeta().getStatus())) {
                    Log.e("StoryDetailActivity", "Failed to save reading history: " + (response.message() != null ? response.message() : "Unknown error"));
                    Toast.makeText(StoryDetailActivity.this, "Không thể lưu lịch sử đọc", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserStoryHistoryResponse>> call, Throwable t) {
                Log.e("StoryDetailActivity", "API call failed: " + t.getMessage());
                Toast.makeText(StoryDetailActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchStoryDetails(String slugName) {
        binding.progressBar.setVisibility(View.VISIBLE);
        RetrofitClient.getStoryApiService().getStory(slugName).enqueue(new Callback<ApiResponse<Story>>() {
            @Override
            public void onResponse(Call<ApiResponse<Story>> call, Response<ApiResponse<Story>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Story> apiResponse = response.body();
                    if ("SUCCESS".equals(apiResponse.getMeta().getStatus())) {
                        Story story = apiResponse.getData();
                        if (story != null) {
                            displayStoryDetails(story);
                        } else {
                            Toast.makeText(StoryDetailActivity.this, "Không tìm thấy truyện", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(StoryDetailActivity.this, "Lỗi API: Trạng thái không hợp lệ", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } else {
                    Toast.makeText(StoryDetailActivity.this, "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Story>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(StoryDetailActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void fetchChapterPaths(String slugName) {
        RetrofitClient.getStoryApiService().getAllChapters(slugName).enqueue(new Callback<ApiResponse<List<ChapterResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ChapterResponse>>> call, Response<ApiResponse<List<ChapterResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<ChapterResponse>> apiResponse = response.body();
                    if ("SUCCESS".equals(apiResponse.getMeta().getStatus())) {
                        List<ChapterResponse> chapterResponse = apiResponse.getData();
                        if (chapterResponse != null) {
                            chapterPaths.clear();
                            for (ChapterResponse chapter : chapterResponse) {
                                chapterPaths.add(chapter.getChapterData());
                            }
                            java.util.Collections.reverse(chapterPaths);
                            Log.d("StoryDetailActivity", "Fetched chapterPaths: " + chapterPaths);
                            updateReadButton();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ChapterResponse>>> call, Throwable t) {
                Toast.makeText(StoryDetailActivity.this, "Lỗi khi lấy danh sách chapter: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayStoryDetails(Story story) {
        binding.storyTitle.setText(story.getName());
        binding.storyStatus.setText("Trạng thái: " + (story.getStatus() != null ? story.getStatus() : "N/A"));
        binding.storyLastUpdated.setText("Cập nhật: " + (story.getUpdatedAt() != null ? story.getUpdatedAt() : "N/A"));
        binding.storyCategories.setText("Thể loại: " + (story.getCategories() != null ?
                story.getCategories().stream().map(Category::getName).collect(Collectors.joining(", ")) : "N/A"));
        binding.storyContent.setText(story.getContent() != null ?
                Html.fromHtml(story.getContent(), Html.FROM_HTML_MODE_COMPACT) : "N/A");

        String thumbnailUrl = IMAGE_BASE_URL + story.getThumbnail();
        Glide.with(this)
                .load(thumbnailUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(binding.storyThumbnail);

        if (story.getChapters() != null && !story.getChapters().isEmpty()) {
            chapters = story.getChapters().get(0).getServerData();
            chapterAdapter.submitList(chapters);
        } else {
            chapters.clear();
            chapterAdapter.submitList(new ArrayList<>());
        }
    }
}