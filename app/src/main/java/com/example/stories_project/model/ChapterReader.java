package com.example.stories_project.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChapterReader {
    private String id;
    private String chapterName;
    private String chapterPath;
    private List<ChapterImage> chapterImages;
}
