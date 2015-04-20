package byteshaft.com.recorder;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.VideoView;


public class VideoPlayer extends Activity implements MediaPlayer.OnCompletionListener,
        Button.OnClickListener, View.OnTouchListener, VideoControllerView.MediaPlayerControl {

    private VideoOverlay mVideoOverlay = null;
    private String videoPath = null;
    private VideoView videoView = null;
    private float preValue = 0;
    private boolean clicked = false;
    private static boolean isLandscape = true;
    VideoControllerView videoControllerView;

    private static class Screen {
        static class Brightness {
            static final float HIGH = 1f;
            static final float DEFAULT = -1f;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        videoControllerView = new VideoControllerView(this);
        Bundle bundle = getIntent().getExtras();
        videoPath = bundle.getString("videoUri");
        videoView = (VideoView) findViewById(R.id.videoSurface);
        videoView.setOnCompletionListener(this);
        videoView.setOnTouchListener(this);
        mVideoOverlay = new VideoOverlay(getApplicationContext());
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
        float brightness = getCurrentBrightness();
        if (event.getX() < v.getWidth() / 2 && event.getY() > 0) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    preValue = event.getY();
                    clicked = true;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (event.getRawY() > preValue) {
                        System.out.println("Going DOWN");
                        brightness-=0.010;
                        setScreenBrightness(brightness);
                    } else {
                        System.out.println("Going UP");
                        brightness+=0.005;
                        setScreenBrightness(brightness);
                    }
                    clicked = false;
                    return true;
                case MotionEvent.ACTION_UP:
                    if (videoControllerView.isShowing()) {
                        videoControllerView.hide();
                    } else {
                        videoControllerView.show();
                    }
                    return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.overlayButton:
                videoView.pause();
                mVideoOverlay.setVideoFile(videoPath);
                mVideoOverlay.setVideoStartPosition(videoView.getCurrentPosition());
                mVideoOverlay.startPlayback();
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
        if (value <= 0.010 || value > 1) {
            return;
        }
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
    public void toggleFullScreen() {    }
}
