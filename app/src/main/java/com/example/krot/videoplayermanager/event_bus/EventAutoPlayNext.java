package com.example.krot.videoplayermanager.event_bus;

import com.example.krot.videoplayermanager.viewholder.VideoItemViewHolder;

/**
 * Created by Krot on 1/31/18.
 */

public class EventAutoPlayNext {

    private final int currentVideoItemPosition;

    public EventAutoPlayNext(int currentVideoItemPosition) {
        this.currentVideoItemPosition = currentVideoItemPosition;
    }

    public int getCurrentVideoItemPosition() {
        return currentVideoItemPosition;
    }

}
