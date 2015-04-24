package byteshaft.com.recorder;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.widget.MediaController;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;


public class VideoPlayer extends Activity implements MediaPlayer.OnCompletionListener,
        Button.OnClickListener, View.OnTouchListener {


    private String videoPath = null;
    private CustomVideoView customVideoView = null;
    private boolean clicked = false;
    private boolean isLandscape = true;
    private float relevantMoveStep = 0;
    private float initialTouchY = 0;
    private Helpers mHelpers = null;

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
        final Button overlayButton = (Button) findViewById(R.id.overlayButton);
        final Button rotationButton = (Button) findViewById(R.id.bRotate);
        overlayButton.setOnClickListener(this);
        rotationButton.setOnClickListener(this);
        Bundle bundle = getIntent().getExtras();
        videoPath = bundle.getString("videoUri");
        customVideoView = (CustomVideoView) findViewById(R.id.videoSurface);
        customVideoView.setOnCompletionListener(this);
        layout.setOnTouchListener(this);
        mHelpers.setScreenBrightness(getWindow(), Screen.Brightness.HIGH);
        MediaController mediaController = new MediaController(this) {
            @Override
            public void show() {
                super.show();
                overlayButton.setVisibility(VISIBLE);
                rotationButton.setVisibility(VISIBLE);
            }

            @Override
            public void hide() {
                super.hide();
                overlayButton.setVisibility(INVISIBLE);
                rotationButton.setVisibility(INVISIBLE);
            }
        };
        mediaController.setAnchorView(customVideoView);
        customVideoView.setMediaController(mediaController);
        customVideoView.setVideoPath(videoPath);
        customVideoView.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        customVideoView.pause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        customVideoView.stopPlayback();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final double BRIGHTNESS_STEP = 0.066;
        final int VOLUME_STEP = 1;
        final int ACTIVITY_HEIGHT_FRAGMENT = v.getHeight() / 50;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialTouchY = event.getY();
                clicked = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                float touchX = event.getX();
                float touchY = event.getY();
                // If action is on the left half of the View.
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
                if (touchY > initialTouchY + 10) {
                    clicked = false;
                } else if (touchY < initialTouchY - 10) {
                    clicked = false;
                }
                return true;
            case MotionEvent.ACTION_UP:
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.overlayButton:
                customVideoView.pause();
                VideoOverlay videoOverlay = new VideoOverlay(getApplicationContext());
                videoOverlay.setVideoFile(customVideoView.getVideoURI());
                videoOverlay.setVideoStartPosition(customVideoView.getCurrentPosition());
                videoOverlay.startPlayback();
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
}
