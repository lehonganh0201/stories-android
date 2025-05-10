package com.example.stories_project.network.response;

import java.time.LocalDateTime;

public record UserStoryFavoriteResponse(
        String username,
        String storySlug,
        String storyName,
        String status,
        String updatedAt,
        String thumbnail,
        String favoritedAt
) {
}
