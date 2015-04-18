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
import android.widget.VideoView;


public class VideoPlayer extends Activity implements MediaPlayer.OnCompletionListener,
        Button.OnClickListener, View.OnTouchListener {

    private VideoOverlay mVideoOverlay = null;
    private String videoPath = null;
    private VideoView videoView = null;
    private float preValue = 0;
    private boolean clicked = false;
    private static boolean isLandscape = true;
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
        initialization();
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
                    if (clicked) {
                        if (videoView.isPlaying()) {
                            videoView.pause();
                        } else {
                            videoView.start();
                        }
                    }
                    preValue = 0;
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
                System.out.println(isLandscape);
                if (isLandscape) {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    isLandscape = false;

                } else {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    isLandscape = true;
                }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        finish();
    }

    private void initialization() {
        Bundle bundle = getIntent().getExtras();
        videoPath = bundle.getString("videoUri");
        videoView = (VideoView) findViewById(R.id.videoSurface);
        videoView.setOnCompletionListener(this);
        videoView.setOnTouchListener(this);
        Button button = (Button) findViewById(R.id.overlayButton);
        button.setOnClickListener(this);
        Button  orientation = (Button) findViewById(R.id.bRotate);
        orientation.setOnClickListener(this);
        mVideoOverlay = new VideoOverlay(getApplicationContext());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setScreenBrightness(Screen.Brightness.HIGH);
        android.widget.MediaController controller = new android.widget.MediaController(this);
        videoView.setMediaController(controller);
        controller.setAnchorView(findViewById(R.id.videoSurface));
        videoView.setVideoPath(videoPath);
        videoView.start();
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
}
