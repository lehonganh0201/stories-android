//package com.example.stories_project.databinding;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//import androidx.recyclerview.widget.RecyclerView;
//import com.example.stories_project.R;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.constraintlayout.widget.ConstraintLayout;
//import androidx.viewbinding.ViewBinding;
//
//public class ActivityChapterReaderBinding implements ViewBinding {
//    public final TextView chapterName;
//    public final RecyclerView imageUrlRecyclerView;
//    private final ConstraintLayout rootView;
//
//    private ActivityChapterReaderBinding(ConstraintLayout rootView, TextView chapterName, RecyclerView imageUrlRecyclerView) {
//        this.rootView = rootView;
//        this.chapterName = chapterName;
//        this.imageUrlRecyclerView = imageUrlRecyclerView;
//    }
//
//    @Override
//    @NonNull
//    public ConstraintLayout getRoot() {
//        return rootView;
//    }
//
//    @NonNull
//    public static ActivityChapterReaderBinding inflate(@NonNull LayoutInflater inflater) {
//        return inflate(inflater, null, false);
//    }
//
//    @NonNull
//    public static ActivityChapterReaderBinding inflate(@NonNull LayoutInflater inflater, @Nullable ViewGroup parent, boolean attachToParent) {
//        View root = inflater.inflate(R.layout.activity_chapter_reader, parent, false);
//        if (attachToParent) {
//            parent.addView(root);
//        }
//        return bind(root);
//    }
//
//    @NonNull
//    public static ActivityChapterReaderBinding bind(@NonNull View rootView) {
//        TextView chapterName = rootView.findViewById(R.id.chapterName);
//        RecyclerView imageUrlRecyclerView = rootView.findViewById(R.id.imageUrlRecyclerView);
//        return new ActivityChapterReaderBinding((ConstraintLayout) rootView, chapterName, imageUrlRecyclerView);
//    }
//}