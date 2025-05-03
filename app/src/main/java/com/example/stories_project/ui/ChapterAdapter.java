package com.example.stories_project.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stories_project.R;
import com.example.stories_project.model.Chapter;

import java.util.ArrayList;
import java.util.List;

public class ChapterAdapter extends RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder> {
    private List<Chapter> chapters = new ArrayList<>();
    private final OnChapterClickListener listener;

    public interface OnChapterClickListener {
        void onChapterClick(Chapter chapter);
    }

    public ChapterAdapter(OnChapterClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chapter_item_layout, parent, false);
        return new ChapterViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ChapterViewHolder holder, int position) {
        Chapter chapter = chapters.get(position);
        holder.bind(chapter);
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    public void submitList(List<Chapter> newChapters) {
        chapters.clear();
        chapters.addAll(newChapters);
        notifyDataSetChanged();
    }

    class ChapterViewHolder extends RecyclerView.ViewHolder {
        private final TextView chapterName;

        ChapterViewHolder(@NonNull View itemView, OnChapterClickListener listener) {
            super(itemView);
            chapterName = itemView.findViewById(R.id.chapterName);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onChapterClick(chapters.get(position));
                }
            });
        }

        void bind(Chapter chapter) {
            chapterName.setText("Chương " + chapter.getChapterName() +
                    (chapter.getChapterTitle() != null && !chapter.getChapterTitle().isEmpty() ?
                            ": " + chapter.getChapterTitle() : ""));
        }
    }
}