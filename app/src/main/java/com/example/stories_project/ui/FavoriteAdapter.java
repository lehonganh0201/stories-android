package com.example.stories_project.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.stories_project.R;
import com.example.stories_project.network.response.UserStoryFavoriteResponse;

public class FavoriteAdapter extends ListAdapter<UserStoryFavoriteResponse, FavoriteAdapter.FavoriteViewHolder> {

    private final OnItemClickListener onItemClickListener;

    private static final String IMAGE_BASE_URL = "https://img.otruyenapi.com/uploads/comics/";

    public interface OnItemClickListener {
        void onItemClick(UserStoryFavoriteResponse favorite);
    }

    public FavoriteAdapter(OnItemClickListener onItemClickListener) {
        super(DIFF_CALLBACK);
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_favorite_layout, parent, false);
        return new FavoriteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        UserStoryFavoriteResponse favorite = getItem(position);
        holder.bind(favorite, onItemClickListener);
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        private final ImageView thumbnailImageView;
        private final TextView storyNameTextView;
        private final TextView statusTextView;
        private final TextView updatedAtTextView;

        FavoriteViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.thumbnailImageView);
            storyNameTextView = itemView.findViewById(R.id.storyNameTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            updatedAtTextView = itemView.findViewById(R.id.updatedAtTextView);
        }

        void bind(UserStoryFavoriteResponse favorite, OnItemClickListener listener) {
            storyNameTextView.setText(favorite.storyName());
            statusTextView.setText("Trạng thái: " + formatStatus(favorite.status()));
            updatedAtTextView.setText("Cập nhật: " + favorite.updatedAt());

            String thumbnailUrl = IMAGE_BASE_URL + favorite.thumbnail();
            Glide.with(itemView.getContext())
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.error)
                    .into(thumbnailImageView);

            itemView.setOnClickListener(v -> listener.onItemClick(favorite));
        }

        private String formatStatus(String status) {
            switch (status) {
                case "ongoing":
                    return "Đang tiến hành";
                case "coming_soon":
                    return "Sắp ra mắt";
                case "completed":
                    return "Hoàn thành";
                default:
                    return status;
            }
        }
    }

    private static final DiffUtil.ItemCallback<UserStoryFavoriteResponse> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<UserStoryFavoriteResponse>() {
                @Override
                public boolean areItemsTheSame(@NonNull UserStoryFavoriteResponse oldItem, @NonNull UserStoryFavoriteResponse newItem) {
                    return oldItem.storySlug().equals(newItem.storySlug());
                }

                @Override
                public boolean areContentsTheSame(@NonNull UserStoryFavoriteResponse oldItem, @NonNull UserStoryFavoriteResponse newItem) {
                    return oldItem.storySlug().equals(newItem.storySlug()) &&
                            oldItem.storyName().equals(newItem.storyName()) &&
                            oldItem.status().equals(newItem.status()) &&
                            oldItem.updatedAt().equals(newItem.updatedAt()) &&
                            oldItem.thumbnail().equals(newItem.thumbnail());
                }
            };
}