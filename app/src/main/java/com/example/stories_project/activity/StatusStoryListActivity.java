package com.example.stories_project.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.stories_project.R;
import com.example.stories_project.adapter.StatusPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class StatusStoryListActivity extends AppCompatActivity {

    private StatusPagerAdapter statusPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status_story_list);

        int selectedTabIndex = getIntent().getIntExtra("tabIndex", 0);

        TextView titleTextView = findViewById(R.id.titleTextView);
        ImageButton searchButton = findViewById(R.id.searchButton);
        ImageButton bellButton = findViewById(R.id.bellButton);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        ViewPager2 viewPager = findViewById(R.id.statusViewPager);

        titleTextView.setText("Danh sách truyện");

        statusPagerAdapter = new StatusPagerAdapter(this);
        viewPager.setAdapter(statusPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(statusPagerAdapter.getTabTitle(position));
        }).attach();

        viewPager.setCurrentItem(selectedTabIndex);

        searchButton.setOnClickListener(v ->
                {
                    Intent intent = new Intent(StatusStoryListActivity.this, SearchActivity.class);
                    startActivity(intent);
                }
        );
        bellButton.setOnClickListener(v -> showToast("Chức năng thông báo chưa được triển khai"));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}