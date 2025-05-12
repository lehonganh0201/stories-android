package com.example.stories_project.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.stories_project.R;
import com.example.stories_project.databinding.ActivityReadingHistoryBinding;
import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.network.RetrofitClient;
import com.example.stories_project.network.response.ChapterResponse;
import com.example.stories_project.network.response.UserStoryHistoryResponse;
import com.example.stories_project.ui.HistoryAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReadingHistoryActivity extends AppCompatActivity {
    private ActivityReadingHistoryBinding binding;
    private HistoryAdapter historyAdapter;
    private static final String PREF_NAME = "UserPrefs";
    private static final String KEY_USERNAME = "username";
    private static final String TAG = "ReadingHistoryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityReadingHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        historyAdapter = new HistoryAdapter(this::fetchChapterPathsAndNavigate);
        binding.historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.historyRecyclerView.setAdapter(historyAdapter);

        fetchReadingHistory();
    }

    private void fetchReadingHistory() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        String username = prefs.getString(KEY_USERNAME, "");
        if (username.isEmpty()) {
            Toast.makeText(this, "Vui lòng đăng nhập để xem lịch sử đọc!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.historyRecyclerView.setVisibility(View.GONE);
        binding.emptyHistoryTextView.setVisibility(View.GONE);

        RetrofitClient.getStoryApiService().getUserHistory(username).enqueue(new Callback<ApiResponse<List<UserStoryHistoryResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<UserStoryHistoryResponse>>> call, Response<ApiResponse<List<UserStoryHistoryResponse>>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null && "SUCCESS".equals(response.body().getMeta().getStatus())) {
                    List<UserStoryHistoryResponse> historyList = response.body().getData();
                    if (historyList != null && !historyList.isEmpty()) {
                        historyAdapter.submitList(historyList);
                        binding.historyRecyclerView.setVisibility(View.VISIBLE);
                        binding.emptyHistoryTextView.setVisibility(View.GONE);
                    } else {
                        binding.historyRecyclerView.setVisibility(View.GONE);
                        binding.emptyHistoryTextView.setVisibility(View.VISIBLE);
                    }
                } else {
                    binding.historyRecyclerView.setVisibility(View.GONE);
                    binding.emptyHistoryTextView.setVisibility(View.VISIBLE);
                    Toast.makeText(ReadingHistoryActivity.this, "Lỗi khi lấy lịch sử đọc", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<UserStoryHistoryResponse>>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.historyRecyclerView.setVisibility(View.GONE);
                binding.emptyHistoryTextView.setVisibility(View.VISIBLE);
                Log.e(TAG, "Failed to fetch reading history: " + t.getMessage());
                Toast.makeText(ReadingHistoryActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchChapterPathsAndNavigate(UserStoryHistoryResponse history) {
        String slugName = history.storySlug();
        int lastChapter = history.lastChapter();

        RetrofitClient.getStoryApiService().getAllChapters(slugName).enqueue(new Callback<ApiResponse<List<ChapterResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ChapterResponse>>> call, Response<ApiResponse<List<ChapterResponse>>> response) {
                if (response.isSuccessful() && response.body() != null && "SUCCESS".equals(response.body().getMeta().getStatus())) {
                    List<ChapterResponse> chapterResponse = response.body().getData();
                    if (chapterResponse != null && !chapterResponse.isEmpty()) {
                        List<String> chapterPaths = new ArrayList<>();
                        for (ChapterResponse chapter : chapterResponse) {
                            chapterPaths.add(chapter.getChapterData());
                        }
                        java.util.Collections.reverse(chapterPaths); // Đảo ngược để mới nhất lên đầu

                        int chapterIndex = chapterPaths.size() - lastChapter;
                        if (chapterIndex >= 0 && chapterIndex < chapterPaths.size()) {
                            String chapterData = chapterPaths.get(chapterIndex);
                            Intent intent = new Intent(ReadingHistoryActivity.this, ChapterReaderActivity.class);
                            intent.putExtra("chapterData", chapterData);
                            intent.putExtra("slugName", slugName);
                            intent.putStringArrayListExtra("chapterPaths", new ArrayList<>(chapterPaths));
                            startActivity(intent);
                        } else {
                            Toast.makeText(ReadingHistoryActivity.this, "Chương không hợp lệ", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ReadingHistoryActivity.this, "Không tìm thấy danh sách chương", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ReadingHistoryActivity.this, "Lỗi khi lấy danh sách chương", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ChapterResponse>>> call, Throwable t) {
                Log.e(TAG, "Failed to fetch chapter paths: " + t.getMessage());
                Toast.makeText(ReadingHistoryActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}