package com.example.stories_project.network.request;

public record UserHistoryRequest(
        String username,

        String storySlug,

        Integer chapter
) {
}
