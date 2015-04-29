package byteshaft.com.recorder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.widget.MediaController;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class VideoPlayer extends Activity implements MediaPlayer.OnCompletionListener,
        View.OnClickListener {

    private CustomVideoView mCustomVideoView;
    private boolean isLandscape = true;
    private Helpers mHelpers;
    private Button mOverlayButton;
    private Button mRotationButton;
    private GestureDetectorCompat mDetector;
    private ScreenStateListener mScreenStateListener;

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
        mOverlayButton = (Button) findViewById(R.id.overlayButton);
        mRotationButton = (Button) findViewById(R.id.bRotate);
        mOverlayButton.setOnClickListener(this);
        mRotationButton.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();
        String videoPath = bundle.getString("videoUri");
        int seekPosition = bundle.getInt("startPosition");
        mCustomVideoView = (CustomVideoView) findViewById(R.id.videoSurface);
        mCustomVideoView.setOnCompletionListener(this);
        mHelpers.setScreenBrightness(getWindow(), Screen.Brightness.HIGH);
        CustomMediaController mediaController = new CustomMediaController(this);
        mediaController.setAnchorView(mCustomVideoView);
        mCustomVideoView.setMediaController(mediaController);
        registerReceiver(mScreenStateListener, filter);
        mCustomVideoView.setVideoPath(videoPath);
        mCustomVideoView.seekTo(seekPosition);
        mCustomVideoView.start();
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

//    @Override
//    protected void onStop() {
//        super.onStop();
//        mCustomVideoView.stopPlayback();
//    }

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
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        finish();
    }

    class CustomMediaController extends MediaController {

        public CustomMediaController(Context context) {
            super(context);
        }

        @Override
        public void show() {
            super.show();
            mOverlayButton.setVisibility(VISIBLE);
            mRotationButton.setVisibility(VISIBLE);
        }

        @Override
        public void hide() {
            super.hide();
            mOverlayButton.setVisibility(INVISIBLE);
            mRotationButton.setVisibility(INVISIBLE);
        }
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
}
