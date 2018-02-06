package com.example.krot.videoplayermanager.viewholder;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.RequestManager;
import com.example.krot.videoplayermanager.CustomThumbnailImageView;
import com.example.krot.videoplayermanager.R;
import com.example.krot.videoplayermanager.event_bus.EventPressPlayButton;
import com.example.krot.videoplayermanager.event_bus.RxBus;
import com.example.krot.videoplayermanager.model.VideoItem;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by Krot on 1/29/18.
 */

public class VideoItemViewHolder extends ItemBaseViewHolder<VideoItem> {

    @BindView(R.id.thumbnail_container)
    public FrameLayout thumbnailContainer;
    @BindView(R.id.portrait_background)
    public CustomThumbnailImageView customPortraitBackground;
    @BindView(R.id.ic_play)
    public ImageView iconPlay;
    @BindView(R.id.video_view)
    public FrameLayout videoView;

    @NonNull
    private final RxBus bus;

    @NonNull
    private RequestManager glideManager;

    public VideoItemViewHolder(ViewGroup parent, int resId, @NonNull RxBus bus, @NonNull RequestManager glideManager) {
        super(parent, resId);
        this.bus = bus;
        this.glideManager = glideManager;
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void bindData(@Nullable VideoItem item) {
        super.bindData(item);
        //do something here
        glideManager.load(ContextCompat.getDrawable(customPortraitBackground.getContext(), R.drawable.landscape_1)).into(customPortraitBackground);
    }

    @OnClick(R.id.thumbnail_container)
    public void doPlayVideo() {
        bus.send(new EventPressPlayButton(getAdapterPosition(), VideoItemViewHolder.this, item));
    }
}
