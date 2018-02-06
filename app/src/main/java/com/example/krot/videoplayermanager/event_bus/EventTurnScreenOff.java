package com.example.krot.videoplayermanager.event_bus;

/**
 * Created by Krot on 1/31/18.
 */

public class EventTurnScreenOff {

    private boolean isScreenOff;

    public EventTurnScreenOff(boolean isScreenOff) {
        this.isScreenOff = isScreenOff;
    }

    public boolean isScreenOff() {
        return isScreenOff;
    }
}
