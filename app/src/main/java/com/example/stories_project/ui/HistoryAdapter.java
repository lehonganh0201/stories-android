package com.example.stories_project.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.stories_project.R;
import com.example.stories_project.network.response.UserStoryHistoryResponse;

import java.util.ArrayList;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    private static final String IMAGE_BASE_URL = "https://img.otruyenapi.com/uploads/comics/";
    private List<UserStoryHistoryResponse> historyList = new ArrayList<>();
    private final OnHistoryClickListener clickListener;

    public interface OnHistoryClickListener {
        void onHistoryClick(UserStoryHistoryResponse history);
    }

    public HistoryAdapter(OnHistoryClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void submitList(List<UserStoryHistoryResponse> historyList) {
        this.historyList = historyList != null ? new ArrayList<>(historyList) : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reading_history_layout, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        UserStoryHistoryResponse history = historyList.get(position);
        holder.storyNameTextView.setText(history.storyName() != null ? history.storyName() : "N/A");
        holder.lastChapterTextView.setText("Chương cuối: " + history.lastChapter());
        holder.lastReadAtTextView.setText("Đọc lúc: " + (history.lastReadAt() != null ? history.lastReadAt() : "N/A"));

        String thumbnailUrl = history.thumbnail() != null ? IMAGE_BASE_URL + history.thumbnail() : "";
        Glide.with(holder.itemView.getContext())
                .load(thumbnailUrl)
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .into(holder.thumbnailImageView);

        holder.itemView.setOnClickListener(v -> clickListener.onHistoryClick(history));
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImageView;
        TextView storyNameTextView;
        TextView lastChapterTextView;
        TextView lastReadAtTextView;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.thumbnailImageView);
            storyNameTextView = itemView.findViewById(R.id.storyNameTextView);
            lastChapterTextView = itemView.findViewById(R.id.lastChapterTextView);
            lastReadAtTextView = itemView.findViewById(R.id.lastReadAtTextView);
        }
    }
}