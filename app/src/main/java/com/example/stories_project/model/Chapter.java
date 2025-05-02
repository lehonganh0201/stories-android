package com.example.stories_project.model;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Chapter {
    @SerializedName("filename")
    private String filename;

    @SerializedName("chapter_name")
    private String chapterName;

    @SerializedName("chapter_title")
    private String chapterTitle;

    @SerializedName("chapter_api_data")
    private String chapterApiData;
}
