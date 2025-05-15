package com.example.stories_project.network.request;

public record UpdateUserRequest(
        String fullName,

        String phoneNumber,

        String dateOfBirth,

        String gender
) {
}
