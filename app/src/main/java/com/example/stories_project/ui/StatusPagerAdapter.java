package com.example.stories_project.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.stories_project.fragment.StoryStatusFragment;

public class StatusPagerAdapter extends FragmentStateAdapter {

    private final String[] tabTitles = {"Truyện mới", "Sắp ra mắt", "Đang phát hành", "Hoàn thành"};
    private final String[] tabTags = {"truyen-moi", "sap-ra-mat", "dang-phat-hanh", "hoan-thanh"};

    public StatusPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return StoryStatusFragment.newInstance(tabTags[position], tabTitles[position]);
    }

    @Override
    public int getItemCount() {
        return tabTitles.length;
    }

    public String getTabTitle(int position) {
        return tabTitles[position];
    }
}