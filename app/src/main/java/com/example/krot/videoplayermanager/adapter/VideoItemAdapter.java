package com.example.krot.videoplayermanager.adapter;

import android.support.annotation.NonNull;
import android.view.ViewGroup;

import com.bumptech.glide.RequestManager;
import com.example.krot.videoplayermanager.R;
import com.example.krot.videoplayermanager.event_bus.RxBus;
import com.example.krot.videoplayermanager.viewholder.ItemBaseViewHolder;
import com.example.krot.videoplayermanager.viewholder.VideoItemViewHolder;

/**
 * Created by Krot on 1/29/18.
 */

public class VideoItemAdapter extends ItemBaseAdapter {
    @NonNull
    private RequestManager glideManager;
    @NonNull
    private RxBus bus;

    public VideoItemAdapter(@NonNull RequestManager glideManager, @NonNull RxBus bus) {
        this.glideManager = glideManager;
        this.bus = bus;
    }

    @Override
    public ItemBaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VideoItemViewHolder(parent, R.layout.video_item, bus, glideManager);
    }
}
