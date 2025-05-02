package com.example.stories_project.ui;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.stories_project.R;
import com.example.stories_project.databinding.ItemStoryBinding;
import com.example.stories_project.model.Category;
import com.example.stories_project.model.Story;

import java.util.ArrayList;
import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.StoryViewHolder> {
    private List<Story> stories = new ArrayList<>();
    private OnItemClickListener listener;
    private static final String IMAGE_BASE_URL = "https://img.otruyenapi.com/uploads/comics/";

    public interface OnItemClickListener {
        void onItemClick(Story story);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Story> newStories) {
        stories.clear();
        stories.addAll(newStories);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemStoryBinding binding = ItemStoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new StoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull StoryViewHolder holder, int position) {
        holder.bind(stories.get(position));
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    class StoryViewHolder extends RecyclerView.ViewHolder {
        private ItemStoryBinding binding;

        public StoryViewHolder(ItemStoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                        listener.onItemClick(stories.get(getAdapterPosition()));
                    }
                }
            });
        }

        public void bind(Story story) {
            binding.storyName.setText(story.getName());

            List<String> categoryNames = new ArrayList<>();
            for (Category category : story.getCategories()) {
                categoryNames.add(category.getName());
            }
            binding.storyCategories.setText(String.join(", ", categoryNames));

            binding.storyStatus.setText("Status: " + story.getStatus());

            String thumbnailUrl = IMAGE_BASE_URL + story.getThumbnail();
            Log.d("StoryAdapter", "Loading image: " + thumbnailUrl);
            Glide.with(binding.getRoot().getContext())
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            Log.e("Glide", "Failed to load image: " + thumbnailUrl + ", error: " + (e != null ? e.getMessage() : "Unknown"));
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            Log.d("Glide", "Image loaded successfully: " + thumbnailUrl);
                            return false;
                        }
                    })
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // Bật caching
                    .into(binding.storyThumbnail);
        }
    }
}
