package com.example.stories_project.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChapterWrapper {
    @SerializedName("server_data")
    private List<Chapter> serverData;
}