package com.example.stories_project.network.response;

public record UserResponse(
        String fullName,
        String phoneNumber,
        String dateOfBirth,
        String gender,
        String avatarUrl
) {
}