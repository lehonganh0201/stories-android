package com.example.stories_project.network.request;

public record UserFavoriteRequest(
        String username,

        String storySlug
) {
}