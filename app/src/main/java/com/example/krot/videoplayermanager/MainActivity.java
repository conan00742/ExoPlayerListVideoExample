package com.example.krot.videoplayermanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.example.krot.videoplayermanager.adapter.VideoItemAdapter;
import com.example.krot.videoplayermanager.event_bus.EventAutoPlayNext;
import com.example.krot.videoplayermanager.event_bus.EventExitFullScreen;
import com.example.krot.videoplayermanager.event_bus.EventPlayNextVideo;
import com.example.krot.videoplayermanager.event_bus.EventPlayPreviousVideo;
import com.example.krot.videoplayermanager.event_bus.EventPressPlayButton;
import com.example.krot.videoplayermanager.event_bus.EventTurnScreenOff;
import com.example.krot.videoplayermanager.event_bus.EventWatchOnFullScreen;
import com.example.krot.videoplayermanager.event_bus.RxBus;
import com.example.krot.videoplayermanager.model.Item;
import com.example.krot.videoplayermanager.model.Video;
import com.example.krot.videoplayermanager.model.VideoItem;
import com.example.krot.videoplayermanager.viewholder.VideoItemViewHolder;
import com.google.android.exoplayer2.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    BroadcastReceiver headphoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        if (videoManager.player.getPlaybackState() == Player.STATE_READY && videoManager.player.getPlayWhenReady()) {
                            videoManager.pause();
                        }
                        break;
                    case 1:
                        break;
                }
            }
        }
    };


    private static final String URL1 = "http://clips.vorwaerts-gmbh.de/VfE_html5.mp4";
    private static final String URL2 = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4";
    private static final String URL3 = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4";
    private static final String URL4 = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4";
    private static final String URL5 = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4";
    private static final String URL6 = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4";
    private static final String URL7 = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4";
    private static final String URL8 = "http://www.html5videoplayer.net/videos/toystory.mp4";
    private static final String URL9 = "http://html5videoformatconverter.com/data/images/happyfit2.mp4";
    private static final String URL10 = "http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4";

    private static final float MILLISECONDS_PER_INCH = 120f;
    private boolean isPlayNext = false;

    @BindView(R.id.video_recycler_view)
    RecyclerView videoRecyclerView;


    private VideoItemViewHolder videoItemViewHolder;
    private int currentItemPosition;

    private VideoManager videoManager;
    private VideoItemAdapter videoItemAdapter;
    private RecyclerView.SmoothScroller smoothScroller;
    private Disposable disposable;
    private RxBus bus;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        bus = new RxBus();
        initAdapter();
        videoManager = new VideoManager(this, bus, videoItemAdapter.getItemCount() - 1);
        videoRecyclerView.addOnScrollListener(videoManager);
        videoRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (isPlayNext) {
                        videoItemViewHolder = (VideoItemViewHolder) videoRecyclerView.findViewHolderForAdapterPosition(currentItemPosition);
                        VideoItem nextVideoItem = (VideoItem) videoItemAdapter.getItemAt(currentItemPosition);
                        videoManager.playVideo(nextVideoItem.getVideo().getVideoUrl(), videoItemViewHolder, currentItemPosition);
                        isPlayNext = false;
                    }
                }
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headphoneReceiver, intentFilter);
        disposable = bus.toObserverable().subscribe(new Consumer<Object>() {
            @Override
            public void accept(Object o) throws Exception {

                //user press play
                if (o instanceof EventPressPlayButton) {
                    EventPressPlayButton eventPressPlayButton = (EventPressPlayButton) o;
                    VideoItemViewHolder viewHolder = eventPressPlayButton.getVideoItemViewHolder();
                    videoManager.playVideo(eventPressPlayButton.getCurrentVideoItem().getVideo().getVideoUrl(), viewHolder, eventPressPlayButton.getPosition());
                }


                //when video ends in NORMAL MODE, auto play next video
                else if (o instanceof EventAutoPlayNext) {
                    EventAutoPlayNext eventAutoPlayNext = (EventAutoPlayNext) o;
                    int currentPosition = eventAutoPlayNext.getCurrentVideoItemPosition();
                    if (currentPosition < videoItemAdapter.getItemCount() - 1) {
                        currentItemPosition = currentPosition + 1;
                        LinearLayoutManager manager = (LinearLayoutManager) videoRecyclerView.getLayoutManager();
                        isPlayNext = true;
                        smoothScroller.setTargetPosition(currentItemPosition);
                        manager.startSmoothScroll(smoothScroller);

                    }

                }


                //when video ends in FULLSCREEN, or user presses play next video
                else if (o instanceof EventPlayNextVideo) {
                    EventPlayNextVideo eventPlayNextVideo = (EventPlayNextVideo) o;
                    int currentPosition = eventPlayNextVideo.getCurrentVideoItemPosition();
                    if (currentPosition < videoItemAdapter.getItemCount() - 1) {
                        currentItemPosition = currentPosition + 1;
                        LinearLayoutManager manager = (LinearLayoutManager) videoRecyclerView.getLayoutManager();

                        //play next video ...
                        VideoItem nextVideoItem = (VideoItem) videoItemAdapter.getItemAt(currentItemPosition);
                        videoManager.playVideoInFullScreen(nextVideoItem.getVideo().getVideoUrl(), currentItemPosition);

                        //... and actively scroll outside
                        smoothScroller.setTargetPosition(currentItemPosition);
                        manager.startSmoothScroll(smoothScroller);
                    }
                }



                //when user presses play previous video
                else if (o instanceof EventPlayPreviousVideo) {
                    EventPlayPreviousVideo eventPlayPreviousVideo = (EventPlayPreviousVideo) o;
                    int currentPosition = eventPlayPreviousVideo.getCurrentVideoItemPosition();
                    if (currentPosition < videoItemAdapter.getItemCount()) {
                        currentItemPosition = currentPosition - 1;
                        LinearLayoutManager manager = (LinearLayoutManager) videoRecyclerView.getLayoutManager();

                        //actively scroll the the previous position ...
                        manager.scrollToPositionWithOffset(currentItemPosition, 0);

                        //... and play previous video
                        VideoItem nextVideoItem = (VideoItem) videoItemAdapter.getItemAt(currentItemPosition);
                        videoManager.playVideoInFullScreen(nextVideoItem.getVideo().getVideoUrl(), currentItemPosition);



                    }
                }

                else if (o instanceof EventWatchOnFullScreen) {
                    EventWatchOnFullScreen eventWatchOnFullScreen = (EventWatchOnFullScreen) o;
                    videoItemViewHolder = (VideoItemViewHolder) videoRecyclerView.findViewHolderForAdapterPosition(eventWatchOnFullScreen.getPosition());
                    videoManager.openFullScreenDialog(videoItemViewHolder);
                }

                else if (o instanceof EventExitFullScreen) {
                    EventExitFullScreen eventExitFullScreen = (EventExitFullScreen) o;
                    videoItemViewHolder = (VideoItemViewHolder) videoRecyclerView.findViewHolderForAdapterPosition(eventExitFullScreen.getPosition());
                    videoManager.closeFullScreenDialog(videoItemViewHolder);
                }


                //when video pause -> screen can sleep and vice versa
                else if (o instanceof EventTurnScreenOff) {
                    EventTurnScreenOff eventTurnScreenOff = (EventTurnScreenOff) o;
                    if (!eventTurnScreenOff.isScreenOff()) {
                        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    } else {
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        videoManager.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(headphoneReceiver);
        videoManager.pause();
        disposable.dispose();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoManager.releasePlayer();
    }


    public void initAdapter() {
        videoItemAdapter = new VideoItemAdapter(Glide.with(this), bus);
        videoItemAdapter.setItemList(generateVideoListItem(getVideoList()));
        videoRecyclerView.setAdapter(videoItemAdapter);

        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        smoothScroller = new LinearSmoothScroller(MainActivity.this) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
            }

        };
        videoRecyclerView.setLayoutManager(manager);
        videoRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }


    public List<Item> generateVideoListItem(List<Video> videoList) {
        List<Item> itemList = new ArrayList<>();
        List<VideoItem> videoItemList = new ArrayList<>();
        for (int i = 0; i < videoList.size(); i++) {
            videoItemList.add(new VideoItem(videoList.get(i)));
        }
        itemList.addAll(videoItemList);
        return itemList;
    }


    public List<Video> getVideoList() {
        List<Video> videoList = new ArrayList<>();

        videoList.add(new Video(UUID.randomUUID().toString(), URL1));
        videoList.add(new Video(UUID.randomUUID().toString(), URL2));
        videoList.add(new Video(UUID.randomUUID().toString(), URL3));
        videoList.add(new Video(UUID.randomUUID().toString(), URL4));
        videoList.add(new Video(UUID.randomUUID().toString(), URL5));
        videoList.add(new Video(UUID.randomUUID().toString(), URL6));
        videoList.add(new Video(UUID.randomUUID().toString(), URL7));
        videoList.add(new Video(UUID.randomUUID().toString(), URL8));
        videoList.add(new Video(UUID.randomUUID().toString(), URL9));
        videoList.add(new Video(UUID.randomUUID().toString(), URL10));

        return videoList;
    }


}
