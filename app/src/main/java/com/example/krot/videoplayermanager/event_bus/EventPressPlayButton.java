package com.example.krot.videoplayermanager.event_bus;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.krot.videoplayermanager.model.VideoItem;
import com.example.krot.videoplayermanager.viewholder.VideoItemViewHolder;

/**
 * Created by Krot on 1/29/18.
 */

public class EventPressPlayButton {

    private final int position;

    @NonNull
    private final VideoItemViewHolder videoItemViewHolder;
    @Nullable
    private final VideoItem currentVideoItem;

    public EventPressPlayButton(int position, @NonNull VideoItemViewHolder videoItemViewHolder, @Nullable VideoItem currentVideoItem) {
        this.position = position;
        this.videoItemViewHolder = videoItemViewHolder;
        this.currentVideoItem = currentVideoItem;
    }

    public int getPosition() {
        return position;
    }

    @NonNull
    public VideoItemViewHolder getVideoItemViewHolder() {
        return videoItemViewHolder;
    }

    @Nullable
    public VideoItem getCurrentVideoItem() {
        return currentVideoItem;
    }
}
