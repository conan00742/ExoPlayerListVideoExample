package com.example.krot.videoplayermanager.event_bus;

/**
 * Created by Krot on 2/6/18.
 */

public class EventExitFullScreen {

    private int position;

    public EventExitFullScreen(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
