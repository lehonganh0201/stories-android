package com.example.stories_project.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.stories_project.MainActivity;
import com.example.stories_project.R;
import com.example.stories_project.ui.StatusPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class StatusStoryListActivity extends AppCompatActivity {

    private StatusPagerAdapter statusPagerAdapter;
    private PopupWindow popupWindow;
    private boolean isMenuShowing = false;
    private static final String PREF_NAME = "UserPrefs";
    private View overlayView;

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

        searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(StatusStoryListActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        bellButton.setOnClickListener(v -> {
            if (isMenuShowing) {
                dismissMenu();
            } else {
                showProfileMenu();
            }
        });
    }

    private void showProfileMenu() {
        View menuView = LayoutInflater.from(this).inflate(R.layout.menu_profile, null);

        popupWindow = new PopupWindow(menuView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        popupWindow.setElevation(8f);

        overlayView = new View(this);
        overlayView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlayView.setBackgroundColor(0x20000000);
        overlayView.setClickable(true);

        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        decorView.addView(overlayView);

        overlayView.setOnClickListener(v -> dismissMenu());

        menuView.findViewById(R.id.menu_account_info).setOnClickListener(v -> {
            // TODO: Navigate to AccountInfoActivity
            Intent intent = new Intent(StatusStoryListActivity.this, AccountInfoActivity.class);
            startActivity(intent);
            dismissMenu();
        });

        menuView.findViewById(R.id.menu_favorites).setOnClickListener(v -> {
            // TODO: Navigate to FavoritesActivity
            Intent intent = new Intent(StatusStoryListActivity.this, FavoritesActivity.class);
            startActivity(intent);
            dismissMenu();
        });

        menuView.findViewById(R.id.menu_reading_history).setOnClickListener(v -> {
            // TODO: Navigate to ReadingHistoryActivity
            Intent intent = new Intent(StatusStoryListActivity.this, ReadingHistoryActivity.class);
            startActivity(intent);
            dismissMenu();
        });

        menuView.findViewById(R.id.menu_logout).setOnClickListener(v -> {
            logout();
            dismissMenu();
        });

        ImageButton bellButton = findViewById(R.id.bellButton);
        int[] location = new int[2];
        bellButton.getLocationOnScreen(location);
        popupWindow.showAtLocation(findViewById(android.R.id.content), Gravity.NO_GRAVITY, location[0], location[1] + bellButton.getHeight());
        isMenuShowing = true;

        popupWindow.setOnDismissListener(() -> {
            if (overlayView.getParent() != null) {
                ((ViewGroup) overlayView.getParent()).removeView(overlayView);
            }
            isMenuShowing = false;
        });
    }

    private void dismissMenu() {
        if (popupWindow != null && popupWindow.isShowing()) {
            popupWindow.dismiss();
        }
    }

    private void logout() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();

        Intent intent = new Intent(StatusStoryListActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Đã đăng xuất", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissMenu();
    }
}