package byteshaft.com.recorder;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;


public class VideoPlayer extends Activity implements SurfaceHolder.Callback,
        MediaPlayer.OnCompletionListener, SurfaceView.OnLongClickListener,
        MediaPlayer.OnPreparedListener {

    private Uri uri = null;
    private MediaPlayer mMediaPlayer = null;
    private VideoOverlay mVideoOverlay = null;
    private String videoPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Bundle bundle = getIntent().getExtras();
        videoPath = bundle.getString("videoUri");
        uri = Uri.parse(videoPath);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        SurfaceView mSurfaceView = (SurfaceView) findViewById(R.id.display);
        mSurfaceView.setOnLongClickListener(this);
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.addCallback(this);
        mVideoOverlay = new VideoOverlay(getApplicationContext());
        setScreenBrightness(Screen.Brightness.HIGH);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Helpers helpers = new Helpers(getApplicationContext());
        helpers.prepareMediaPlayer(mMediaPlayer, uri, holder);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        setScreenBrightness(Screen.Brightness.DEFAULT);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        finish();
    }

    @Override
    public boolean onLongClick(View v) {
        mMediaPlayer.pause();
        mVideoOverlay.setVideoFile(videoPath);
        mVideoOverlay.setVideoStartPosition(mMediaPlayer.getCurrentPosition());
        mVideoOverlay.startPlayback();
        return false;
    }

    private void setScreenBrightness(float value) {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = value;
        getWindow().setAttributes(layoutParams);
    }

    private static class Screen {
        static class Brightness {
            static final float HIGH = 1f;
            static final float DEFAULT = -1f;
        }
    }
}
