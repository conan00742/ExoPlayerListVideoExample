package com.example.krot.videoplayermanager.model;

import android.support.annotation.NonNull;

/**
 * Created by Krot on 1/29/18.
 */

public class Video {

    @NonNull
    private final String videoId;

    @NonNull
    private final String videoUrl;


    public Video(@NonNull String videoId, @NonNull String videoUrl) {
        this.videoId = videoId;
        this.videoUrl = videoUrl;
    }

    @NonNull
    public String getVideoId() {
        return videoId;
    }

    @NonNull
    public String getVideoUrl() {
        return videoUrl;
    }


}
