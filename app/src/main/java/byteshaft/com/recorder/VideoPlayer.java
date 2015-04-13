package byteshaft.com.recorder;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;


public class VideoPlayer extends Activity implements SurfaceHolder.Callback,
        MediaPlayer.OnCompletionListener, SurfaceView.OnLongClickListener {

    private SurfaceView mSurfaceView = null;
    Helpers mHelper = null;
    private Uri uri = null;
    private MediaPlayer mMediaPlayer = null;
    private VideoOverlay mVideoOverlay = null;
    private String videoPath = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        Bundle bundle = getIntent().getExtras();
        videoPath = bundle.getString("videoUri");
        uri = Uri.parse(videoPath);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        mSurfaceView = (SurfaceView) findViewById(R.id.display);
        mSurfaceView.setOnLongClickListener(this);
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.addCallback(this);
        mVideoOverlay = new VideoOverlay(getApplicationContext());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHelper = new Helpers(getApplicationContext());
        mHelper.playVideo(mMediaPlayer, uri, holder);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

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
}
