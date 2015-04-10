package byteshaft.com.recorder;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;


public class VideoActivity extends ActionBarActivity implements SurfaceHolder.Callback {
    SurfaceView videoDisply;
    SurfaceHolder mSurfaceHolder;
    String fileRepo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        videoDisply = (SurfaceView) findViewById(R.id.surfaceView);
        mSurfaceHolder = videoDisply.getHolder();
        mSurfaceHolder.addCallback(this);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        fileRepo = bundle.getString("file");



    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        System.out.println("surface Created");
        playVideo();


    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void playVideo() {
        Uri uri1 = Uri.parse(fileRepo);
        MediaPlayer mediaPlayer = new MediaPlayer();
        if (mediaPlayer.isPlaying()){
            mediaPlayer.reset();
        }

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDisplay(mSurfaceHolder);

        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri1);
            mediaPlayer.prepare();
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.start();
    }
}
