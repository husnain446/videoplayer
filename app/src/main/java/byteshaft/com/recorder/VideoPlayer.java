package byteshaft.com.recorder;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.widget.MediaController;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;


public class VideoPlayer extends Activity implements MediaPlayer.OnCompletionListener,
        View.OnClickListener, View.OnTouchListener {

    private CustomVideoView mCustomVideoView = null;
    private boolean clicked = false;
    private boolean isLandscape = true;
    private float initialTouchY = 0;
    private Helpers mHelpers = null;
    private Button mOverlayButton = null;
    private Button mRotationButton = null;

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
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.videoLayout);
        mOverlayButton = (Button) findViewById(R.id.overlayButton);
        mRotationButton = (Button) findViewById(R.id.bRotate);
        mOverlayButton.setOnClickListener(this);
        mRotationButton.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();
        String videoPath = bundle.getString("videoUri");
        mCustomVideoView = (CustomVideoView) findViewById(R.id.videoSurface);
        mCustomVideoView.setOnCompletionListener(this);
        layout.setOnTouchListener(this);
        mHelpers.setScreenBrightness(getWindow(), Screen.Brightness.HIGH);
        MediaController mediaController = new CustomMediaController(this);
        mediaController.setAnchorView(mCustomVideoView);
        mCustomVideoView.setMediaController(mediaController);
        mCustomVideoView.setVideoPath(videoPath);
        mCustomVideoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCustomVideoView.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCustomVideoView.stopPlayback();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final double BRIGHTNESS_STEP = 0.066;
        final int VOLUME_STEP = 1;
        final int ACTIVITY_HEIGHT_FRAGMENT = v.getHeight() / 50;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialTouchY = event.getY();
                setClicked(true);
                return true;
            case MotionEvent.ACTION_MOVE:
                float touchX = event.getX();
                float touchY = event.getY();
                // If action is on the left half of the View.
                float relevantMoveStep;
                if (touchX < v.getWidth() / 2) {
                    float brightness = mHelpers.getCurrentBrightness(getWindow());
                    if (touchY > initialTouchY &&
                            brightness - BRIGHTNESS_STEP > Screen.Brightness.LOW) {
                        System.out.println("Going DOWN");
                        relevantMoveStep = initialTouchY + ACTIVITY_HEIGHT_FRAGMENT;
                        if (touchY >= relevantMoveStep) {
                            brightness -= BRIGHTNESS_STEP;
                            mHelpers.setScreenBrightness(getWindow(), brightness);
                            initialTouchY = event.getY();
                        }
                    } else if (touchY < initialTouchY &&
                            brightness + BRIGHTNESS_STEP <= Screen.Brightness.HIGH) {
                        System.out.println("Going UP");
                        relevantMoveStep = initialTouchY - ACTIVITY_HEIGHT_FRAGMENT;
                        if (touchY <= relevantMoveStep) {
                            brightness += BRIGHTNESS_STEP;
                            mHelpers.setScreenBrightness(getWindow(), brightness);
                            initialTouchY = event.getY();
                        }
                    }
                } else {
                    int volume = mHelpers.getCurrentVolume();
                    if (touchY > initialTouchY &&
                            volume - VOLUME_STEP >= Sound.Level.MINIMUM) {
                        relevantMoveStep = initialTouchY + ACTIVITY_HEIGHT_FRAGMENT;
                        if (touchY >= relevantMoveStep) {
                            volume -= VOLUME_STEP;
                            mHelpers.setVolume(volume);
                            initialTouchY = event.getY();
                        }
                    } else if (touchY < initialTouchY &&
                            volume + VOLUME_STEP <= Sound.Level.MAXIMUM) {
                        relevantMoveStep = initialTouchY - ACTIVITY_HEIGHT_FRAGMENT;
                        if (touchY <= relevantMoveStep) {
                            volume += VOLUME_STEP;
                            mHelpers.setVolume(volume);
                            initialTouchY = event.getY();
                        }
                    }
                }
                if (touchY > initialTouchY + 10 || touchY < initialTouchY - 10) {
                    setClicked(false);
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (isClicked()) {
                    // Do something if it was a tap.
                }
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.overlayButton:
                mCustomVideoView.pause();
                VideoOverlay videoOverlay = new VideoOverlay(getApplicationContext());
                videoOverlay.setVideoFile(mCustomVideoView.getVideoURI());
                videoOverlay.setVideoStartPosition(mCustomVideoView.getCurrentPosition());
                videoOverlay.setVideoHeight(mCustomVideoView.getVideoHeight());
                videoOverlay.setVideoWidth(mCustomVideoView.getVideoWidth());
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
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        finish();
    }

    public boolean isClicked() {
        return clicked;
    }

    public void setClicked(boolean clicked) {
        this.clicked = clicked;
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
}
