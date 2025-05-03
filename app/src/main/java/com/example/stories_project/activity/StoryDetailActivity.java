package com.example.stories_project.activity;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.stories_project.R;
import com.example.stories_project.databinding.ActivityStoryDetailBinding;
import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.model.Category;
import com.example.stories_project.model.Story;
import com.example.stories_project.network.RetrofitClient;
import com.example.stories_project.ui.ChapterAdapter;

import java.util.ArrayList;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoryDetailActivity extends AppCompatActivity {
    private ActivityStoryDetailBinding binding;
    private ChapterAdapter chapterAdapter;

    private static final String IMAGE_BASE_URL = "https://img.otruyenapi.com/uploads/comics/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStoryDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        chapterAdapter = new ChapterAdapter(chapter -> {
            Toast.makeText(this, "Đã chọn chương: " + chapter.getChapterName(), Toast.LENGTH_SHORT).show();
        });
        binding.chapterRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.chapterRecyclerView.setAdapter(chapterAdapter);

        String slugName = getIntent().getStringExtra("slugName");
        if (slugName == null) {
            Toast.makeText(this, "Không tìm thấy slugName", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchStoryDetails(slugName);
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

    private void displayStoryDetails(Story story) {
        binding.storyTitle.setText(story.getName());
        binding.storyStatus.setText("Trạng thái: " + (story.getStatus() != null ? story.getStatus() : "N/A"));
        binding.storyLastUpdated.setText("Cập nhật: " + (story.getUpdatedAt() != null ? story.getUpdatedAt() : "N/A"));
        binding.storyCategories.setText("Thể loại: " + (story.getCategories() != null ?
                story.getCategories().stream().map(Category::getName).collect(Collectors.joining(", ")) : "N/A"));
        binding.storyContent.setText(story.getContent() != null ?
                Html.fromHtml(story.getContent(), Html.FROM_HTML_MODE_COMPACT) : "N/A");

        String thumbnailUrl = IMAGE_BASE_URL+ story.getThumbnail();
        Glide.with(this)
                .load(thumbnailUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(binding.storyThumbnail);

        if (story.getChapters() != null && !story.getChapters().isEmpty()) {
            chapterAdapter.submitList(story.getChapters().get(0).getServerData());
        } else {
            chapterAdapter.submitList(new ArrayList<>());
        }
    }
}