package com.example.stories_project.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.stories_project.R;
import com.example.stories_project.databinding.ActivityChapterReaderBinding;
import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.model.ChapterImage;
import com.example.stories_project.model.ChapterReader;
import com.example.stories_project.network.RetrofitClient;
import com.example.stories_project.network.request.ChapterRequest;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChapterReaderActivity extends AppCompatActivity {
    private ActivityChapterReaderBinding binding;
    private ImageAdapter imageAdapter;
    private static final String IMAGE_BASE_URL = "https://sv1.otruyencdn.com/";
    private static final String TAG = "ChapterReaderActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChapterReaderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo RecyclerView và Adapter
        imageAdapter = new ImageAdapter();
        binding.imageUrlRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.imageUrlRecyclerView.setAdapter(imageAdapter);
        binding.imageUrlRecyclerView.setHasFixedSize(true); // Tối ưu hiệu suất

        // Lấy dữ liệu chapterData từ Intent
        String chapterData = getIntent().getStringExtra("chapterData");
        if (chapterData == null || chapterData.isEmpty()) {
            Log.e(TAG, "chapterData is null or empty");
            Toast.makeText(this, "Không tìm thấy dữ liệu chương", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d(TAG, "Received chapterData: " + chapterData);

        binding.chapterName.setText("Đang tải...");

        fetchChapterDetails(chapterData);
    }

    private void fetchChapterDetails(String chapterData) {
        binding.imageUrlRecyclerView.setVisibility(View.GONE);
        ChapterRequest request = new ChapterRequest(chapterData);
        Log.d(TAG, "Fetching chapter details for path: " + chapterData);
        RetrofitClient.getStoryApiService().getChapterDetail(request).enqueue(new Callback<ApiResponse<ChapterReader>>() {
            @Override
            public void onResponse(Call<ApiResponse<ChapterReader>> call, Response<ApiResponse<ChapterReader>> response) {
                binding.imageUrlRecyclerView.setVisibility(View.VISIBLE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ChapterReader> apiResponse = response.body();
                    Log.d(TAG, "API response status: " + apiResponse.getMeta().getStatus());
                    if ("SUCCESS".equals(apiResponse.getMeta().getStatus())) {
                        ChapterReader chapterReader = apiResponse.getData();
                        if (chapterReader != null && chapterReader.getChapterImages() != null) {
                            Log.d(TAG, "Received " + chapterReader.getChapterImages().size() + " images");
                            binding.chapterName.setText("AntiCP");
                            imageAdapter.setImages(chapterReader);
                        } else {
                            Log.e(TAG, "ChapterReader or images is null");
                            imageAdapter.setImages(null);
                            Toast.makeText(ChapterReaderActivity.this, "Không có hình ảnh nào trong chương", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "API status not SUCCESS: " + apiResponse.getMeta().getStatus());
                        Toast.makeText(ChapterReaderActivity.this, "Lỗi API: Trạng thái không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "API response failed: " + (response.message() != null ? response.message() : "Unknown error"));
                    Toast.makeText(ChapterReaderActivity.this, "Lỗi: " + (response.message() != null ? response.message() : "Unknown error"), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ChapterReader>> call, Throwable t) {
                binding.imageUrlRecyclerView.setVisibility(View.VISIBLE);
                Log.e(TAG, "API call failed: " + t.getMessage());
                Toast.makeText(ChapterReaderActivity.this, "Lỗi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
        private List<ChapterImage> chapterImages = new ArrayList<>();
        private ChapterReader chapter;

        void setImages(ChapterReader chapter) {
            this.chapter = chapter;
            this.chapterImages = chapter != null && chapter.getChapterImages() != null ? chapter.getChapterImages() : new ArrayList<>();
            Log.d(TAG, "Setting " + chapterImages.size() + " images to adapter");
            notifyDataSetChanged();
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chapter_image, parent, false);
            return new ImageViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            ChapterImage chapterImage = chapterImages.get(position);
            String imageUrl = chapterImage.getImageFile() != null && chapter != null && chapter.getChapterPath() != null
                    ? IMAGE_BASE_URL + chapter.getChapterPath() + "/" + chapterImage.getImageFile()
                    : "";
            Log.d(TAG, "Image URL [" + position + "]: " + imageUrl);
            Glide.with(ChapterReaderActivity.this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "Glide load failed for URL: " + imageUrl + ", error: " + (e != null ? e.getMessage() : "Unknown"));
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d(TAG, "Glide loaded successfully for URL: " + imageUrl);
                            return false;
                        }
                    })
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return chapterImages.size();
        }

        class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            ImageViewHolder(View itemView) {
                super(itemView);
                imageView = itemView.findViewById(R.id.chapterImage);
            }
        }
    }
}