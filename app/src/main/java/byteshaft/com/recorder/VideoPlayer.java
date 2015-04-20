package byteshaft.com.recorder;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.VideoView;


public class VideoPlayer extends Activity implements MediaPlayer.OnCompletionListener,
        Button.OnClickListener, View.OnTouchListener, VideoControllerView.MediaPlayerControl {


    private String videoPath = null;
    private VideoView videoView = null;
    private boolean clicked = false;
    VideoControllerView videoControllerView;
    private boolean isLandscape = true;
    private float relevantMoveStep = 0;
    private float initialTouchY = 0;

    private static class Screen {
        static class Brightness {
            static final float HIGH = 1f;
            static final float DEFAULT = -1f;
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
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.videoLayout);
        videoControllerView = new VideoControllerView(this);
        Bundle bundle = getIntent().getExtras();
        videoPath = bundle.getString("videoUri");
        videoView = (VideoView) findViewById(R.id.videoSurface);
        videoView.setOnCompletionListener(this);
        layout.setOnTouchListener(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setScreenBrightness(Screen.Brightness.HIGH);
        videoControllerView.setMediaPlayer(this);
        videoControllerView.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));
        videoView.setVideoPath(videoPath);
        videoView.start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        setScreenBrightness(Screen.Brightness.DEFAULT);
        videoView.stopPlayback();
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
                    float brightness = getCurrentBrightness();
                    if (touchY > initialTouchY &&
                            brightness - BRIGHTNESS_STEP > Screen.Brightness.LOW) {
                        System.out.println("Going DOWN");
                        relevantMoveStep = initialTouchY + ACTIVITY_HEIGHT_FRAGMENT;
                        if (touchY >= relevantMoveStep) {
                            brightness -= BRIGHTNESS_STEP;
                            setScreenBrightness(brightness);
                            initialTouchY = event.getY();
                        }
                    } else if (touchY < initialTouchY &&
                            brightness + BRIGHTNESS_STEP <= Screen.Brightness.HIGH) {
                        System.out.println("Going UP");
                        relevantMoveStep = initialTouchY - ACTIVITY_HEIGHT_FRAGMENT;
                        if (touchY <= relevantMoveStep) {
                            brightness += BRIGHTNESS_STEP;
                            setScreenBrightness(brightness);
                            initialTouchY = event.getY();
                        }
                    }
                } else {
                    int volume = getCurrentVolume();
                    if (touchY > initialTouchY &&
                            volume - VOLUME_STEP >= Sound.Level.MINIMUM) {
                        relevantMoveStep = initialTouchY + ACTIVITY_HEIGHT_FRAGMENT;
                        if (touchY >= relevantMoveStep) {
                            volume -= VOLUME_STEP;
                            setVolume(volume);
                            initialTouchY = event.getY();
                        }
                    } else if (touchY < initialTouchY &&
                            volume + VOLUME_STEP <= Sound.Level.MAXIMUM) {
                        relevantMoveStep = initialTouchY - ACTIVITY_HEIGHT_FRAGMENT;
                        if (touchY <= relevantMoveStep) {
                            volume += VOLUME_STEP;
                            setVolume(volume);
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
                if (clicked) {
                    if (videoControllerView.isShowing()) {
                        videoControllerView.hide();
                    } else {
                        videoControllerView.show();
                    }
                }
                return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.overlayButton:
                videoView.pause();
                VideoOverlay videoOverlay = new VideoOverlay(getApplicationContext());
                videoOverlay.setVideoFile(videoPath);
                videoOverlay.setVideoStartPosition(videoView.getCurrentPosition());
                videoOverlay.startPlayback();
                finish();
                showDesktop();
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

    private void setScreenBrightness(float value) {
        System.out.println(String.format("Attempted value %f", value));
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = value;
        getWindow().setAttributes(layoutParams);
    }

    private void showDesktop() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    private float getCurrentBrightness() {
        return getWindow().getAttributes().screenBrightness;
    }

    private void togglePlayback() {
        if (videoView.isPlaying()) {
            videoView.pause();
        } else {
            videoView.start();
        }
    }

    @Override
    public boolean canPause() {
        return true;
    }

    @Override
    public boolean canSeekBackward() {
        return true;
    }

    @Override
    public boolean canSeekForward() {
        return true;
    }

    @Override
    public int getBufferPercentage() {
        return 0;
    }

    @Override
    public int getCurrentPosition() {
        return videoView.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return videoView.getDuration();
    }

    @Override
    public boolean isPlaying() {
        return videoView.isPlaying();
    }

    @Override
    public void pause() {
        videoView.pause();
    }

    @Override
    public void seekTo(int i) {
        videoView.seekTo(i);
    }

    @Override
    public void start() {
        videoView.start();
    }

    @Override
    public boolean isFullScreen() {
        return false;
    }

    @Override
    public void toggleFullScreen() {

    }

    private int getCurrentVolume() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        return am.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    private void setVolume(int level) {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, level, 0);
    }
}
