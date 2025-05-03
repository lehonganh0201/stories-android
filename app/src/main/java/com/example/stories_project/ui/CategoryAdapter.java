package com.example.stories_project.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stories_project.R;
import com.example.stories_project.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<Category> categories = new ArrayList<>();
    private final OnCategoryClickListener listener;
    private final int[] backgroundColors = {
            R.color.category_color_1,
            R.color.category_color_2,
            R.color.category_color_3,
            R.color.category_color_4,
            R.color.category_color_5,
            R.color.category_color_6
    };

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public CategoryAdapter(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_item_layout, parent, false);
        return new CategoryViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.bind(category, position);
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
                    listener.onCategoryClick(categories.get(position));
                }
            });
        }

        void bind(Category category, int position) {
            name.setText(category.getName());
            int colorRes = backgroundColors[position % backgroundColors.length];
            name.setBackgroundColor(ContextCompat.getColor(name.getContext(), colorRes));
        }
    }
}