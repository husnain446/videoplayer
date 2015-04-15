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
         VideoView.OnTouchListener, Button.OnClickListener {

    private VideoOverlay mVideoOverlay = null;
    private String videoPath = null;
    private VideoView videoView = null;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (videoView.isPlaying()) {
            videoView.pause();
        } else {
            videoView.start();
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
        Bundle bundle = getIntent().getExtras();
        videoPath = bundle.getString("videoUri");
        videoView = (VideoView) findViewById(R.id.videoSurface);
        videoView.setOnCompletionListener(this);
        videoView.setOnTouchListener(this);
        Button button = (Button) findViewById(R.id.overlayButton);
        button.setOnClickListener(this);
        mVideoOverlay = new VideoOverlay(getApplicationContext());
        setScreenBrightness(Screen.Brightness.HIGH);
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
    public void onCompletion(MediaPlayer mp) {
        finish();
    }

    private void setScreenBrightness(float value) {
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
}
