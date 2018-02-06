package com.example.krot.videoplayermanager.event_bus;

/**
 * Created by Krot on 2/5/18.
 */

public class EventPlayPreviousVideo {

    private final int currentVideoItemPosition;

    public EventPlayPreviousVideo(int currentVideoItemPosition) {
        this.currentVideoItemPosition = currentVideoItemPosition;
    }

    public int getCurrentVideoItemPosition() {
        return currentVideoItemPosition;
    }

}
