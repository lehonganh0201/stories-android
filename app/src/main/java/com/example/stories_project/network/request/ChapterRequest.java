package com.example.stories_project.network.request;

public class ChapterRequest {
    private String chapterData;

    public ChapterRequest(String chapterData) {
        this.chapterData = chapterData;
    }

    public String getChapterData() {
        return chapterData;
    }

    public void setChapterData(String chapterData) {
        this.chapterData = chapterData;
    }
}

