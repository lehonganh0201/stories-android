package com.example.stories_project.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Story {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("slug")
    private String slug;

    @SerializedName("status")
    private String status;

    @SerializedName("updatedAt")
    private String updatedAt;

    @SerializedName("thumbnail")
    private String thumbnail;

    @SerializedName("content")
    private String content;

    @SerializedName("category")
    private List<Category> categories;

    @SerializedName("chapterLastests")
    private Object chapterLastests;

    @SerializedName("chapters")
    private List<ChapterWrapper> chapters;
}