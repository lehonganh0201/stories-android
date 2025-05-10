package com.example.stories_project.network.response;

import java.time.LocalDateTime;

public record UserStoryHistoryResponse(
        String username,
        String storySlug,
        String storyName,
        String status,
        String updatedAt,
        String thumbnail,
        int lastChapter,
        String lastReadAt
) {
}

