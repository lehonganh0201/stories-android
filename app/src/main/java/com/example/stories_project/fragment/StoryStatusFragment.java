package com.example.stories_project.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.stories_project.R;
import com.example.stories_project.model.ApiResponse;
import com.example.stories_project.model.Story;
import com.example.stories_project.network.RetrofitClient;
import com.example.stories_project.ui.StoryAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StoryStatusFragment extends Fragment {

    private static final String ARG_TAG = "tag";
    private static final String ARG_TAG_NAME = "tag_name";

    private RecyclerView recyclerView;
    private StoryAdapter adapter;
    private ProgressBar progressBar;
    private TextView errorTextView;
    private int currentPage = 1;
    private String tag;
    private List<Story> allStories = new ArrayList<>();
    private boolean isLoading = false;
    private boolean isLastPage = false;

    public static StoryStatusFragment newInstance(String tag, String tagName) {
        StoryStatusFragment fragment = new StoryStatusFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAG, tag);
        args.putString(ARG_TAG_NAME, tagName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tag = getArguments().getString(ARG_TAG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_story_status, container, false);

        // Khởi tạo các view
        recyclerView = view.findViewById(R.id.storyRecyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        errorTextView = view.findViewById(R.id.errorTextView);

        // Thiết lập RecyclerView
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new StoryAdapter();
        recyclerView.setAdapter(adapter);

        // Điều chỉnh padding cho RecyclerView
        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int recyclerViewWidth = v.getWidth();
            float density = getResources().getDisplayMetrics().density;
            int cardViewWidth = (int) (160 * density + 16 * density);
            int totalGridWidth = cardViewWidth * 2;
            int extraSpace = recyclerViewWidth - totalGridWidth - (int) (16 * density);
            if (extraSpace > 0) {
                int padding = extraSpace / 2;
                v.setPadding(padding, v.getPaddingTop(), padding, v.getPaddingBottom());
            }
        });

        // Thêm OnScrollListener để phát hiện cuộn đến cuối danh sách
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage && (visibleItemCount + firstVisibleItemPosition) >= totalItemCount - 4) {
                    currentPage++;
                    fetchStoriesByTag(tag, currentPage, false);
                }
            }
        });

        // Gọi API để lấy danh sách truyện lần đầu
        if (tag != null) {
            fetchStoriesByTag(tag, currentPage, true);
        } else {
            showError("Không tìm thấy tag danh mục");
        }

        return view;
    }

    private void fetchStoriesByTag(String tag, int pageNumber, boolean isFirstLoad) {
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);
        errorTextView.setVisibility(View.GONE);
        if (isFirstLoad) {
            recyclerView.setVisibility(View.GONE);
        }

        Call<ApiResponse<List<Story>>> call = RetrofitClient.getStoryApiService().getStories(tag, pageNumber);
        call.enqueue(new Callback<ApiResponse<List<Story>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Story>>> call, Response<ApiResponse<List<Story>>> response) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Story>> apiResponse = response.body();
                    if ("SUCCESS".equals(apiResponse.getMeta().getStatus())) {
                        List<Story> stories = apiResponse.getData();
                        if (stories.isEmpty()) {
                            if (isFirstLoad) {
                                showError("Không có truyện nào trong danh mục này");
                            }
                            isLastPage = true;
                        } else {
                            if (isFirstLoad) {
                                allStories.clear();
                            }
                            allStories.addAll(stories);
                            adapter.submitList(new ArrayList<>(allStories));
                            recyclerView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        showError("Lỗi API: " + apiResponse.getMeta().getStatus());
                    }
                } else {
                    showError("Không thể lấy danh sách truyện");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Story>>> call, Throwable t) {
                isLoading = false;
                progressBar.setVisibility(View.GONE);
                showError("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        errorTextView.setText(message);
        errorTextView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }
}