package com.example.stories_project.model;

import com.google.gson.annotations.SerializedName;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Meta {
    @SerializedName("status")
    private String status;
}
