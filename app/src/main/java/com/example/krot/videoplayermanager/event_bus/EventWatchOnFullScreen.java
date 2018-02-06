package com.example.krot.videoplayermanager.event_bus;

/**
 * Created by Krot on 2/6/18.
 */

public class EventWatchOnFullScreen {

    private int position;

    public EventWatchOnFullScreen(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
