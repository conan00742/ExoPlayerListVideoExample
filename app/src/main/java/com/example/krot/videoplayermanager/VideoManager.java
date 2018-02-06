package com.example.krot.videoplayermanager;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.krot.videoplayermanager.event_bus.EventAutoPlayNext;
import com.example.krot.videoplayermanager.event_bus.EventExitFullScreen;
import com.example.krot.videoplayermanager.event_bus.EventPlayNextVideo;
import com.example.krot.videoplayermanager.event_bus.EventPlayPreviousVideo;
import com.example.krot.videoplayermanager.event_bus.EventTurnScreenOff;
import com.example.krot.videoplayermanager.event_bus.EventWatchOnFullScreen;
import com.example.krot.videoplayermanager.event_bus.RxBus;
import com.example.krot.videoplayermanager.viewholder.VideoItemViewHolder;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.HashMap;


/**
 * TODO LIST:
 * 1. Fullscreen
 * 1a. do full screen
 * 1b. off full screen resume exact state and time of current video
 **/


public class VideoManager extends RecyclerView.OnScrollListener implements Player.EventListener, ImageView.OnClickListener, AudioManager.OnAudioFocusChangeListener {

    private static final int FAST_FORWARD_MILLISECONDS = 10000;

    private ImageView iconPlayback;
    private ImageView iconFastForward;
    private ImageView iconRewind;
    private ImageView iconNext;
    private ImageView iconPrevious;
    private ImageView iconMute;
    private ImageView iconFullScreen;
    private ProgressBar progressBarBuffering;
    private boolean isPlaying = false;
    private boolean isFullScreen = false;
    private boolean isMuted = false;
    private float currentVolume = 1;

    private Dialog mFullScreenDialog;

    @NonNull
    private final DataSource.Factory dataSourceFactory;

    @NonNull
    public final SimpleExoPlayer player;

    @Nullable
    private SimpleExoPlayerView exoPlayerView;

    @Nullable
    private FrameLayout videoFrame = null;

    private HashMap<String, Long> durationMap = new HashMap<>();

    @Nullable
    private String previousVideoUrl = "";

    private final Context context;
    private int playbackPosition;
    private int lastVideoIndex;
    private RxBus bus;
    private AudioManager audioManager;
    private AudioFocusRequest mFocusRequest;


    @Nullable
    private VideoItemViewHolder holder;
    private FrameLayout dialogRoot;

    public VideoManager(Context context,
                        final RxBus bus,
                        int lastIndex) {
        this.context = context;
        this.bus = bus;
        lastVideoIndex = lastIndex;
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        dataSourceFactory = new DefaultDataSourceFactory(
                context,
                Util.getUserAgent(context, context.getResources().getString(R.string.app_name)));
        player = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        player.addListener(this);
        initFullScreenDialog();

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(true)
                    .setWillPauseWhenDucked(true)
                    .setOnAudioFocusChangeListener(this)
                    .build();

        }


    }


    private void prepareSource(String videoUrl) {
        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(videoUrl));
        player.prepare(videoSource, false, false);
    }


    public void playVideo(String videoUrl,
                          @Nullable VideoItemViewHolder viewHolder,
                          int currentItemPosition) {
        playbackPosition = currentItemPosition;

        holder = viewHolder;

        checkForThePreviousVideo();
        prepareSource(videoUrl);

        long duration;
        if (durationMap.get(videoUrl) != null) {
            duration = durationMap.get(videoUrl);
            player.seekTo(duration);
        } else {
            player.seekTo(0);
        }

        initExoPlayerView();
        bindChildView();


        if (holder != null) {
            holder.videoView.setBackgroundColor(Color.BLACK);
            holder.videoView.addView(exoPlayerView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            videoFrame = holder.videoView;
        }


        previousVideoUrl = videoUrl;
        exoPlayerView.setPlayer(player);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(mFocusRequest);
        } else {
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }

        play();
    }



    public void playVideoInFullScreen(String videoUrl,
                                      int currentItemPosition) {
        playbackPosition = currentItemPosition;


        checkForThePreviousVideo();
        prepareSource(videoUrl);


        long duration;
        if (durationMap.get(videoUrl) != null) {
            duration = durationMap.get(videoUrl);
            player.seekTo(duration);
        } else {
            player.seekTo(0);
        }

        initExoPlayerView();
        bindChildView();


        dialogRoot.removeAllViewsInLayout();
        dialogRoot.addView(exoPlayerView);


        previousVideoUrl = videoUrl;
        exoPlayerView.setPlayer(player);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(mFocusRequest);
        } else {
            audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        }

        play();
    }


    public void play() {
        player.setPlayWhenReady(true);
    }


    public void pause() {
        player.setPlayWhenReady(false);
    }


    public void releasePlayer() {
        player.release();
    }


    @Override
    public void onScrolled(RecyclerView recyclerView,
                           int dx,
                           int dy) {
        if (videoFrame != null && ((player.getPlaybackState() != Player.STATE_ENDED) || (player.getPlaybackState() == Player.STATE_READY && player.getPlayWhenReady()))) {
            View parentView = (View) videoFrame.getParent();
            int parentMiddle = (parentView.getTop() + parentView.getBottom()) / 2;
            int containerMiddle = (videoFrame.getTop() + videoFrame.getBottom()) / 2;
            if (-parentView.getTop() > containerMiddle || parentMiddle > recyclerView.getMeasuredHeight()) {
                pause();
                isPlaying = false;
                videoFrame.setBackgroundColor(Color.TRANSPARENT);
                videoFrame.removeView(exoPlayerView);
            }
        }
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest) {
        //do what you want here
    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        //do what you want here
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        //do what you want here
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        switch (playbackState) {
            case Player.STATE_IDLE:
                break;
            case Player.STATE_BUFFERING:
                hidePlayback();
                progressBarBuffering.setVisibility(View.VISIBLE);
                iconPlayback.setVisibility(View.GONE);
                break;
            case Player.STATE_READY:
                progressBarBuffering.setVisibility(View.GONE);
                iconPlayback.setVisibility(View.VISIBLE);

                if (isFullScreen) {
                    if (playbackPosition == 0) {
                        iconPrevious.setVisibility(View.GONE);
                        iconNext.setVisibility(View.VISIBLE);
                        iconRewind.setVisibility(View.VISIBLE);
                        iconFastForward.setVisibility(View.VISIBLE);
                    } else if (playbackPosition == lastVideoIndex) {
                        iconNext.setVisibility(View.GONE);
                        iconPrevious.setVisibility(View.VISIBLE);
                        iconRewind.setVisibility(View.VISIBLE);
                        iconFastForward.setVisibility(View.VISIBLE);
                    } else {
                        showPlayback();
                    }
                }


                if (playWhenReady) {
                    isPlaying = true;
                    bus.send(new EventTurnScreenOff(false));
                    iconPlayback.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pause_vector));
                } else {
                    isPlaying = false;
                    bus.send(new EventTurnScreenOff(true));
                    iconPlayback.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play_vector));
                }
                break;
            case Player.STATE_ENDED:
                if (isFullScreen) {
                    showPlayback();
                    bus.send(new EventPlayNextVideo(playbackPosition));
                } else {
                    bus.send(new EventAutoPlayNext(playbackPosition));
                }
                bus.send(new EventTurnScreenOff(true));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    audioManager.abandonAudioFocusRequest(mFocusRequest);
                } else {
                    audioManager.abandonAudioFocus(this);
                }
                isPlaying = false;
                break;
        }

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        //do what you want here
    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
        //do what you want here
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        //do what you want here
    }

    @Override
    public void onPositionDiscontinuity(int reason) {
        //do what you want here
    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        //do what you want here
    }

    @Override
    public void onSeekProcessed() {
        //do what you want here
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            //playback icon
            case R.id.img_playback:
                if (isPlaying) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        audioManager.abandonAudioFocusRequest(mFocusRequest);
                    } else {
                        audioManager.abandonAudioFocus(this);
                    }

                    pause();
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        audioManager.requestAudioFocus(mFocusRequest);
                    } else {
                        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
                    }

                    play();
                }
                break;

            //fullscreen icon
            case R.id.icon_fullscreen:
                if (!isFullScreen) {
                    bus.send(new EventWatchOnFullScreen(playbackPosition));
                } else {
                    bus.send(new EventExitFullScreen(playbackPosition));
                }
                break;

            case R.id.img_fast_forward:
                if (isPlaying) {
                    player.seekTo(player.getCurrentPosition() + FAST_FORWARD_MILLISECONDS);
                    play();
                } else {
                    player.seekTo(player.getCurrentPosition() + FAST_FORWARD_MILLISECONDS);
                    pause();
                }
                break;

            case R.id.img_rewind:
                if (isPlaying) {
                    player.seekTo(player.getCurrentPosition() - FAST_FORWARD_MILLISECONDS);
                    play();
                } else {
                    player.seekTo(player.getCurrentPosition() - FAST_FORWARD_MILLISECONDS);
                    pause();
                }
                break;

            case R.id.img_next:
                bus.send(new EventPlayNextVideo(playbackPosition));
                break;

            case R.id.img_back:
                bus.send(new EventPlayPreviousVideo(playbackPosition));
                break;

            //volume icon
            case R.id.icon_volume:
                if (isMuted) {
                    currentVolume = 1;
                    player.setVolume(currentVolume);
                    iconMute.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.ic_unmute));
                    isMuted = false;
                } else {
                    currentVolume = 0;
                    player.setVolume(currentVolume);
                    iconMute.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.ic_mute));
                    isMuted = true;
                }
                break;

        }

    }


    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_LOSS:
                if (isPlaying) {
                    isPlaying = false;
                    pause();
                    iconPlayback.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play_vector));
                }
                break;

        }
    }


    private void initFullScreenDialog() {
        mFullScreenDialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            @Override
            public void onBackPressed() {
                if (isFullScreen) {
                    bus.send(new EventExitFullScreen(playbackPosition));
                }
                super.onBackPressed();
            }
        };


        dialogRoot = new FrameLayout(context);
        mFullScreenDialog.setContentView(dialogRoot);

    }


    public void openFullScreenDialog(VideoItemViewHolder viewHolder) {
        if (viewHolder != null) {
            viewHolder.videoView.removeView(exoPlayerView);
        }

        dialogRoot.addView(exoPlayerView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        showPlayback();

        if (playbackPosition == 0) {
            iconPrevious.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_back_for_first_video));
            iconPrevious.setEnabled(false);
        } else if (playbackPosition == lastVideoIndex) {
            iconNext.setEnabled(false);
            iconNext.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_next_for_last_video));
        } else {
            iconNext.setEnabled(true);
            iconPrevious.setEnabled(true);
        }

        isFullScreen = true;
        mFullScreenDialog.show();
        iconFullScreen.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.ic_minimize_screen));

    }

    public void closeFullScreenDialog(VideoItemViewHolder viewHolder) {
        dialogRoot.removeView(exoPlayerView);
        if (viewHolder != null) {
            viewHolder.videoView.setBackgroundColor(Color.BLACK);
            viewHolder.videoView.addView(exoPlayerView);
            videoFrame = viewHolder.videoView;
        }

        hidePlayback();

        isFullScreen = false;
        mFullScreenDialog.dismiss();
        iconFullScreen.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.ic_full_screen));
    }

    private void initExoPlayerView() {
        if (holder != null) {
            exoPlayerView = (SimpleExoPlayerView) LayoutInflater.from(context).inflate(R.layout.video_view, this.holder.videoView, false);
            exoPlayerView.setFastForwardIncrementMs(FAST_FORWARD_MILLISECONDS);
        }

    }

    private void bindChildView() {
        //icon playback
        iconPlayback = exoPlayerView.findViewById(R.id.img_playback);
        iconPlayback.setOnClickListener(this);

        //icon fast forward
        iconFastForward = exoPlayerView.findViewById(R.id.img_fast_forward);
        iconFastForward.setOnClickListener(this);

        //icon rewind
        iconRewind = exoPlayerView.findViewById(R.id.img_rewind);
        iconRewind.setOnClickListener(this);

        //icon next
        iconNext = exoPlayerView.findViewById(R.id.img_next);
        iconNext.setOnClickListener(this);

        //icon previous
        iconPrevious = exoPlayerView.findViewById(R.id.img_back);
        iconPrevious.setOnClickListener(this);


        //icon fullscreen
        iconFullScreen = exoPlayerView.findViewById(R.id.icon_fullscreen);
        iconFullScreen.setOnClickListener(this);

        //icon mute
        iconMute = exoPlayerView.findViewById(R.id.icon_volume);
        iconMute.setOnClickListener(this);

        if (isMuted) {
            currentVolume = 0;
            player.setVolume(currentVolume);
            iconMute.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.ic_mute));
        } else {
            currentVolume = 1;
            player.setVolume(currentVolume);
            iconMute.setImageDrawable(ContextCompat.getDrawable(context, R.mipmap.ic_unmute));
        }

        progressBarBuffering = exoPlayerView.findViewById(R.id.progress_bar_buffering);
    }


    private void checkForThePreviousVideo() {
        if (exoPlayerView != null && videoFrame != null) {

            if (player.getPlaybackState() == Player.STATE_IDLE) {
                videoFrame.removeView(exoPlayerView);
                videoFrame.setBackgroundColor(Color.TRANSPARENT);
                videoFrame = null;
                durationMap.put(previousVideoUrl, player.getCurrentPosition());
            } else if (player.getPlaybackState() == Player.STATE_BUFFERING) {
                videoFrame.removeView(exoPlayerView);
                videoFrame.setBackgroundColor(Color.TRANSPARENT);
                videoFrame = null;
                durationMap.put(previousVideoUrl, player.getCurrentPosition());
            }

            //video is playing
            else if (player.getPlaybackState() == Player.STATE_READY && isPlaying) {
                videoFrame.removeView(exoPlayerView);
                videoFrame.setBackgroundColor(Color.TRANSPARENT);
                videoFrame = null;
                pause();
                isPlaying = false;
                durationMap.put(previousVideoUrl, player.getCurrentPosition());
            }

            //video pauses
            else if (player.getPlaybackState() == Player.STATE_READY && !isPlaying) {
                videoFrame.removeView(exoPlayerView);
                videoFrame.setBackgroundColor(Color.TRANSPARENT);
                videoFrame = null;
                durationMap.put(previousVideoUrl, player.getCurrentPosition());
            }

            //video ends
            else if (player.getPlaybackState() == Player.STATE_ENDED) {
                durationMap.remove(previousVideoUrl);
                videoFrame.removeView(exoPlayerView);
                videoFrame.setBackgroundColor(Color.TRANSPARENT);
                videoFrame = null;
            }

        }
    }


    private void hidePlayback() {
        iconFastForward.setVisibility(View.GONE);
        iconRewind.setVisibility(View.GONE);
        iconNext.setVisibility(View.GONE);
        iconPrevious.setVisibility(View.GONE);
    }


    private void showPlayback() {
        iconFastForward.setVisibility(View.VISIBLE);
        iconRewind.setVisibility(View.VISIBLE);
        iconNext.setVisibility(View.VISIBLE);
        iconPrevious.setVisibility(View.VISIBLE);
    }

}