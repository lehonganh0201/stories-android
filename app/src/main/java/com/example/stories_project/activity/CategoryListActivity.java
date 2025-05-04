package com.example.stories_project.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCategoryListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        categoryAdapter = new CategoryListAdapter(category ->
                Toast.makeText(this, "Đã chọn thể loại: " + category.getName(), Toast.LENGTH_SHORT).show()
        );
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(this);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.FLEX_START);
        binding.categoryRecyclerView.setLayoutManager(layoutManager);
        binding.categoryRecyclerView.setAdapter(categoryAdapter);

        binding.searchButton.setOnClickListener(v ->
                {
                    Intent intent = new Intent(CategoryListActivity.this, SearchActivity.class);
                    startActivity(intent);
                }
        );

        fetchCategories();
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
}