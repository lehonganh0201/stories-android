package com.example.stories_project.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
import com.example.stories_project.network.response.ChapterResponse;

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
    private List<String> chapterPaths;
    private int currentChapterIndex;
    private String slugName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChapterReaderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        imageAdapter = new ImageAdapter();
        binding.imageUrlRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.imageUrlRecyclerView.setAdapter(imageAdapter);
        binding.imageUrlRecyclerView.setHasFixedSize(true);

        String chapterData = getIntent().getStringExtra("chapterData");
        slugName = getIntent().getStringExtra("slugName");
        chapterPaths = getIntent().getStringArrayListExtra("chapterPaths");
        if (chapterData == null || chapterData.isEmpty() || slugName == null || chapterPaths == null || chapterPaths.isEmpty()) {
            Log.e(TAG, "chapterData, slugName, or chapterPaths is null or empty");
            Toast.makeText(this, "Không tìm thấy dữ liệu chương", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Log.d(TAG, "Received chapterData: " + chapterData);
        Log.d(TAG, "Received slugName: " + slugName);
        Log.d(TAG, "Received chapterPaths: " + chapterPaths);

        currentChapterIndex = chapterPaths.indexOf(chapterData);
        if (currentChapterIndex == -1) {
            Log.e(TAG, "chapterData not found in chapterPaths");
            Toast.makeText(this, "Chapter không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, getChapterNames());
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.chapterSpinner.setAdapter(spinnerAdapter);
        binding.chapterSpinner.setSelection(currentChapterIndex);

        binding.chapterSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position != currentChapterIndex) {
                    currentChapterIndex = position;
                    fetchChapterDetails(chapterPaths.get(position));
                    binding.imageUrlRecyclerView.scrollToPosition(0);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        binding.prevChapterButton.setOnClickListener(v -> {
            if (currentChapterIndex < chapterPaths.size() - 1) {
                currentChapterIndex++;
                binding.chapterSpinner.setSelection(currentChapterIndex);
                fetchChapterDetails(chapterPaths.get(currentChapterIndex));
                binding.imageUrlRecyclerView.scrollToPosition(0);
            } else {
                fetchNewChapters();
            }
        });

        binding.nextChapterButton.setOnClickListener(v -> {
            if (currentChapterIndex > 0) {
                currentChapterIndex--;
                binding.chapterSpinner.setSelection(currentChapterIndex);
                fetchChapterDetails(chapterPaths.get(currentChapterIndex));
                binding.imageUrlRecyclerView.scrollToPosition(0);
            } else {
                Toast.makeText(this, "Đây là chapter đầu tiên", Toast.LENGTH_SHORT).show();
            }
        });

        binding.chapterName.setText("Đang tải...");

        fetchChapterDetails(chapterData);
    }

    private List<String> getChapterNames() {
        List<String> chapterNames = new ArrayList<>();
        for (int i = 0; i < chapterPaths.size(); i++) {
            chapterNames.add("Chapter " + (chapterPaths.size() - i));
        }
        return chapterNames;
    }

    private void fetchNewChapters() {
        Log.d(TAG, "Fetching new chapters for slugName: " + slugName);
        RetrofitClient.getStoryApiService().getAllChapters(slugName).enqueue(new Callback<ApiResponse<List<ChapterResponse>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<ChapterResponse>>> call, Response<ApiResponse<List<ChapterResponse>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<ChapterResponse>> apiResponse = response.body();
                    if ("SUCCESS".equals(apiResponse.getMeta().getStatus())) {
                        List<ChapterResponse> chapterResponse = apiResponse.getData();
                        if (chapterResponse != null) {
                            List<String> newChapterPaths = new ArrayList<>();
                            for (ChapterResponse chapter : chapterResponse) {
                                newChapterPaths.add(chapter.getChapterData());
                            }

                            if (!newChapterPaths.isEmpty()) {
                                chapterPaths.addAll(newChapterPaths);
                                java.util.Collections.reverse(newChapterPaths); // Đảm bảo chapter mới nhất lên đầu
                                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(ChapterReaderActivity.this, android.R.layout.simple_spinner_item, getChapterNames());
                                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                binding.chapterSpinner.setAdapter(spinnerAdapter);
                                currentChapterIndex++;
                                binding.chapterSpinner.setSelection(currentChapterIndex);
                                fetchChapterDetails(chapterPaths.get(currentChapterIndex));
                                binding.imageUrlRecyclerView.scrollToPosition(0);
                            } else {
                                Toast.makeText(ChapterReaderActivity.this, "Không có chapter mới", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ChapterReaderActivity.this, "Không tìm thấy chapter mới", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ChapterReaderActivity.this, "Lỗi API: Trạng thái không hợp lệ", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ChapterReaderActivity.this, "Lỗi: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<ChapterResponse>>> call, Throwable t) {
                Toast.makeText(ChapterReaderActivity.this, "Lỗi khi lấy chapter mới: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
                            binding.chapterName.setText(chapterReader.getChapterName() != null ? chapterReader.getChapterName() : "N/A");
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
            if (imageUrl.isEmpty()) {
                Log.e(TAG, "Empty image URL at position: " + position);
                holder.imageView.setImageResource(R.drawable.error);
                return;
            }
            Glide.with(ChapterReaderActivity.this)
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e(TAG, "Glide load failed for URL: " + imageUrl + ", error: " + (e != null ? e.getMessage() : "Unknown"));
                            Toast.makeText(ChapterReaderActivity.this, "Không tải được ảnh: " + imageUrl, Toast.LENGTH_SHORT).show();
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
            Log.d(TAG, "Adapter item count: " + chapterImages.size());
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