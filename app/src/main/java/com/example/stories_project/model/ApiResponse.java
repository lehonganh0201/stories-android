package com.example.stories_project.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {
    @SerializedName("meta")
    private Meta meta;

    @SerializedName("data")
    private T data;
}
