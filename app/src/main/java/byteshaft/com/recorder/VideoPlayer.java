package byteshaft.com.recorder;

import android.app.Activity;
import android.content.Intent;
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
    private double preValue = 0;
    private float brightness = 1;
    private boolean clicked = false;

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
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getX() < v.getWidth() / 3) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    preValue = event.getY();
                    clicked = true;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int result = v.getHeight() / 100;
                    if (event.getY() > preValue + result) {
                        System.out.println("Going UP");
                        if (brightness > 0) {
                            brightness-=0.10;
                            setScreenBrightness(brightness);
                            preValue = event.getY();
                        }

                    } else if (event.getY() < preValue + result) {
                        System.out.println("Going DOWN");
                        if (brightness < 1) {
                            brightness+=0.10;
                            setScreenBrightness(brightness);
                            preValue = event.getY();
                        }
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
                    return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        videoView.pause();
        mVideoOverlay.setVideoFile(videoPath);
        mVideoOverlay.setVideoStartPosition(videoView.getCurrentPosition());
        mVideoOverlay.startPlayback();
        finish();
        showDesktop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        setScreenBrightness(Screen.Brightness.DEFAULT);
        videoView.stopPlayback();
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
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        System.out.println(value);
        layoutParams.screenBrightness = value;
        getWindow().setAttributes(layoutParams);
    }

    private void showDesktop() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
}
