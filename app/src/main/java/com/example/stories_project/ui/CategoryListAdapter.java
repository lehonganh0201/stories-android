package com.example.stories_project.ui;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stories_project.R;
import com.example.stories_project.activity.StoryListActivity;
import com.example.stories_project.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryListAdapter extends RecyclerView.Adapter<CategoryListAdapter.CategoryViewHolder> {
    private List<Category> categories = new ArrayList<>();
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryListAdapter(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_list_item_layout, parent, false);
        return new CategoryViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void submitList(List<Category> newCategories) {
        categories.clear();
        categories.addAll(newCategories);
        notifyDataSetChanged();
    }

     class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;

        CategoryViewHolder(@NonNull View itemView, OnCategoryClickListener listener) {
            super(itemView);
            name = itemView.findViewById(R.id.categoryName);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    Category category = categories.get(position);
                    Intent intent = new Intent(itemView.getContext(), StoryListActivity.class);
                    intent.putExtra("categoryName", category.getName());
                    intent.putExtra("categorySlug", category.getSlug());
                    itemView.getContext().startActivity(intent);

                }
            });
        }

        void bind(Category category) {
            name.setText(category.getName());
        }
    }
}