package com.byteshaft.videoplayer;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.widget.ImageButton;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.Random;

public class VideoPlayer extends Activity implements MediaPlayer.OnCompletionListener,
        View.OnClickListener, CustomVideoView.MediaPlayerStateChangedListener,
        SeekBar.OnSeekBarChangeListener {

    private CustomVideoView mCustomVideoView;
    private boolean isLandscape = true;
    private Helpers mHelpers;
    private GestureDetectorCompat mDetector;
    private ScreenStateListener mScreenStateListener;
    private static final int sDefaultTimeout = 3000;
    private SeekBar mSeekBar;
    private TextView mStartTime;
    private TextView mEndTime;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            mCustomVideoView.seekTo(progress);
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private static class Screen {
        static class Brightness {
            static final float HIGH = 1f;
            static final float LOW = 0f;
        }
    }

    private static class Sound {
        static class Level {
            static final int MINIMUM = 0;
            static final int MAXIMUM = 15;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        mHelpers = new Helpers(getApplicationContext());
        mScreenStateListener = new ScreenStateListener(mCustomVideoView);
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        mDetector = new GestureDetectorCompat(this, new GestureListener());
        Button overlayButton = (Button) findViewById(R.id.overlayButton);
        Button rotationButton = (Button) findViewById(R.id.bRotate);
        ToggleButton mButtonPausePlay = (ToggleButton) findViewById(R.id.toggle);
        ImageButton mButtonForward = (ImageButton) findViewById(R.id.button_forward);
        ImageButton mButtonRewind = (ImageButton) findViewById(R.id.button_rewind);
        mSeekBar = (SeekBar) findViewById(R.id.media_controller_progress);
        mStartTime = (TextView) findViewById(R.id.video_staring_time);
        mEndTime = (TextView) findViewById(R.id.video_end_time);
        overlayButton.setOnClickListener(this);
        rotationButton.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();
        String videoPath = bundle.getString("videoUri");
        int seekPosition = bundle.getInt("startPosition");
        int videopos = bundle.getInt("TextView");
        mCustomVideoView = (CustomVideoView) findViewById(R.id.videoSurface);
        mCustomVideoView.setMediaPlayerStateChangedListener(this);
        mCustomVideoView.setOnCompletionListener(this);
        mHelpers.setScreenBrightness(getWindow(), Screen.Brightness.HIGH);
        registerReceiver(mScreenStateListener, filter);
        mCustomVideoView.setVideoPath(videoPath);
        mCustomVideoView.seekTo(seekPosition);
        mCustomVideoView.start();
        mButtonPausePlay.setOnClickListener(this);
        mButtonForward.setOnClickListener(this);
        mButtonRewind.setOnClickListener(this);
        mSeekBar.setOnSeekBarChangeListener(this);
        mStartTime.setText(mHelpers.getFormattedTime(mCustomVideoView.getDuration()));
        mEndTime.setText(mHelpers.getFormattedTime(mHelpers.getDurationForVideo(videopos)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCustomVideoView.pause();
        try {
            unregisterReceiver(mScreenStateListener);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.overlayButton:
                VideoOverlay videoOverlay = new VideoOverlay(getApplicationContext());
                videoOverlay.setVideoFile(mCustomVideoView.getVideoURI());
                videoOverlay.setVideoStartPosition(mCustomVideoView.getCurrentPosition());
                videoOverlay.setVideoHeight(mCustomVideoView.getVideoHeight());
                videoOverlay.setVideoWidth(mCustomVideoView.getVideoWidth());
                videoOverlay.setPlayOnStart(mCustomVideoView.isPlaying());
                mCustomVideoView.pause();
                videoOverlay.startPlayback();
                mCustomVideoView.stopPlayback();
                finish();
                mHelpers.showLauncherHome();
                break;
            case R.id.bRotate:
                if (isLandscape) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                isLandscape = !isLandscape;
                break;
            case R.id.toggle:
                if (mCustomVideoView.isPlaying()) {
                    mCustomVideoView.pause();
                    mSeekBar.postDelayed(onEverySecond, 0);
                    mStartTime.setText(mHelpers.getFormattedTime(mCustomVideoView.getCurrentPosition()));

                } else {
                    mCustomVideoView.start();
                    mStartTime.setText(mHelpers.getFormattedTime(mCustomVideoView.getCurrentPosition()));
                    mSeekBar.postDelayed(onEverySecond, 1000);
                    mSeekBar.postDelayed(onEverySecond, 0);



                }
                break;
            case R.id.button_forward:
                if (mCustomVideoView.isPlaying() && isVideoAtLastThreeSeconds()) {
                    mCustomVideoView.seekTo(mCustomVideoView.getCurrentPosition() + sDefaultTimeout);
                    mSeekBar.postDelayed(onEverySecond, 2000);
                }
                break;
            case R.id.button_rewind:
                if (mCustomVideoView.isPlaying()) {
                    mCustomVideoView.seekTo(mCustomVideoView.getCurrentPosition() - sDefaultTimeout);
                    mSeekBar.postDelayed(onEverySecond, 2000);
                }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (mHelpers.isRepeatEnabled()) {
            mp.start();
        } else if (mHelpers.isShuffleEnabled()){
            Random randomizer = new Random();
            mHelpers.playVideoForLocation(MainActivity.mVideosPathList.get(
                    randomizer.nextInt(MainActivity.mVideosPathList.size())), 0);
        } else {
            finish();
        }
    }

    @Override
    public void onPlaybackStateChanged(int state) {

    }

    @Override
    public void onVideoViewPrepared(MediaPlayer mp) {
        setVideoOrientation();
        mSeekBar.setMax(mCustomVideoView.getDuration());
        mSeekBar.postDelayed(onEverySecond, 1000);
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        final double BRIGHTNESS_STEP = 0.066;
        final int VOLUME_STEP = 1;
        private float lastTrackedPosition;

        @Override
        public boolean onDown(MotionEvent e) {
            lastTrackedPosition = e.getY();
            return super.onDown(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            final int ACTIVITY_HEIGHT_FRAGMENT = getActivityHeight() / 50;
            float touchX = e2.getX();
            float touchY = e2.getY();
            if (touchX < getActivityWidth() / 2) {
                float brightness = mHelpers.getCurrentBrightness(getWindow());
                if (touchY >= lastTrackedPosition + ACTIVITY_HEIGHT_FRAGMENT &&
                        brightness - BRIGHTNESS_STEP > Screen.Brightness.LOW) {
                    brightness -= BRIGHTNESS_STEP;
                    mHelpers.setScreenBrightness(getWindow(), brightness);
                    lastTrackedPosition = touchY;
                } else if (touchY <= lastTrackedPosition - ACTIVITY_HEIGHT_FRAGMENT &&
                        brightness + BRIGHTNESS_STEP <= Screen.Brightness.HIGH) {
                    brightness += BRIGHTNESS_STEP;
                    mHelpers.setScreenBrightness(getWindow(), brightness);
                    lastTrackedPosition = touchY;
                }
            } else {
                int currentVolume = mHelpers.getCurrentVolume();
                if (touchY > lastTrackedPosition + ACTIVITY_HEIGHT_FRAGMENT &&
                        currentVolume - VOLUME_STEP >= Sound.Level.MINIMUM) {
                    currentVolume -= VOLUME_STEP;
                    mHelpers.setVolume(currentVolume);
                    lastTrackedPosition = touchY;
                } else if (touchY <= lastTrackedPosition - ACTIVITY_HEIGHT_FRAGMENT &&
                        currentVolume + VOLUME_STEP <= Sound.Level.MAXIMUM) {
                    currentVolume += VOLUME_STEP;
                    mHelpers.setVolume(currentVolume);
                    lastTrackedPosition = touchY;
                }
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    private int getActivityHeight() {
        return getWindow().getDecorView().getHeight();
    }

    private int getActivityWidth() {
        return getWindow().getDecorView().getWidth();
    }

    private void setVideoOrientation() {
        if (mHelpers.isVideoPortrait(mCustomVideoView.getVideoHeight(),
                mCustomVideoView.getVideoWidth())) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private Runnable onEverySecond = new Runnable() {

        @Override
        public void run() {
            if (mSeekBar != null) {
                mSeekBar.setProgress(mCustomVideoView.getCurrentPosition());
            }

            if (mCustomVideoView.isPlaying()) {
                if (mSeekBar != null) {
                    mSeekBar.postDelayed(onEverySecond, 1000);
                }
                mStartTime.setText(mHelpers.getFormattedTime(mCustomVideoView.getCurrentPosition()));
            }
        }
    };

    private boolean isVideoAtLastThreeSeconds() {
        int currentPosition = mCustomVideoView.getCurrentPosition();
        return currentPosition + 3000 <= mCustomVideoView.getDuration();
    }
}
