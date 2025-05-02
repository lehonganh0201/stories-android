package com.example.stories_project.model;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Category {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("slug")
    private String slug;
}