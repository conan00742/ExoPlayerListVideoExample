package com.example.krot.videoplayermanager.model;

import android.support.annotation.Nullable;

/**
 * Created by Krot on 1/29/18.
 */

public class VideoItem implements Item{

    @Nullable
    private final Video video;

    public VideoItem(Video video) {
        this.video = video;
    }

    @Nullable
    public Video getVideo() {
        return video;
    }
}
