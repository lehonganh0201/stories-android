package com.example.stories_project.ui;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.stories_project.R;
import com.example.stories_project.activity.StoryDetailActivity;
import com.example.stories_project.model.Category;
import com.example.stories_project.model.Story;

import java.util.ArrayList;
import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
    private List<Story> stories = new ArrayList<>();
    private final OnBannerClickListener listener;

    private static final String IMAGE_BASE_URL = "https://img.otruyenapi.com/uploads/comics/";

    public interface OnBannerClickListener {
        void onBannerClick(Story story);
    }

    public BannerAdapter(OnBannerClickListener listener) {
        this.listener = listener;
    }

    public BannerAdapter() {
        this.listener = null;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.banner_item_layout, parent, false);
        return new BannerViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Story story = stories.get(position);
        holder.bind(story);
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    public void submitList(List<Story> newStories) {
        stories.clear();
        stories.addAll(newStories);
        notifyDataSetChanged();
    }

    class BannerViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnail;
        private final TextView name;
        private final TextView status;
        private final TextView lastUpdated;
        private final TextView categories;

        BannerViewHolder(@NonNull View itemView, OnBannerClickListener listener) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.bannerThumbnail);
            name = itemView.findViewById(R.id.bannerName);
            status = itemView.findViewById(R.id.bannerStatus);
            lastUpdated = itemView.findViewById(R.id.bannerLastUpdated);
            categories = itemView.findViewById(R.id.bannerCategories);

            if (listener != null) {
                itemView.setOnClickListener(v -> {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        Story story = stories.get(position);
                        if (listener != null) {
                            listener.onBannerClick(story);
                        }
                        Intent intent = new Intent(itemView.getContext(), StoryDetailActivity.class);
                        intent.putExtra("slugName", story.getSlug());
                        itemView.getContext().startActivity(intent);
                    }
                });
            }
        }

        void bind(Story story) {
            name.setText(story.getName());
            status.setText("Trạng thái: " + (story.getStatus() != null ? story.getStatus() : "Không xác định"));
            lastUpdated.setText("Cập nhật: " + (story.getUpdatedAt() != null ? story.getUpdatedAt() : "Không xác định"));

            List<String> categoryNames = new ArrayList<>();
            for (Category category : story.getCategories()) {
                categoryNames.add(category.getName());
            }
            categories.setText("Thể loại: " + String.join(", ", categoryNames));
            Glide.with(thumbnail.getContext())
                    .load(IMAGE_BASE_URL + story.getThumbnail())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .into(thumbnail);
        }
    }
}