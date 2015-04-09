package byteshaft.com.recorder;

import android.app.Activity;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import java.io.IOException;


public class MainActivity extends Activity implements SurfaceHolder.Callback,
        View.OnClickListener {

    Camera mCamera;
    SurfaceHolder mHolder;
    SurfaceView display;
    MediaRecorder mediaRecorder;
    Button rec, stop, open, play;
    String filePath = (Environment.getExternalStoragePublicDirectory("Example.mp4").getAbsolutePath());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        open = (Button) findViewById(R.id.open);
        rec = (Button) findViewById(R.id.recorder);
        stop = (Button) findViewById(R.id.stop);
        play = (Button) findViewById(R.id.play);
        open.setOnClickListener(this);
        rec.setOnClickListener(this);
        stop.setOnClickListener(this);
        play.setOnClickListener(this);
        display = (SurfaceView) findViewById(R.id.display);
        mHolder = display.getHolder();
        mHolder.addCallback(this);

    }

    private void openCamera() {
        mCamera = Camera.open();
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private void recordVideo() {
        mediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        CamcorderProfile camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
        mediaRecorder.setProfile(camcorderProfile);
        mediaRecorder.setOrientationHint(90);
        mediaRecorder.setOutputFile(filePath);
        try {
            mediaRecorder.setPreviewDisplay(mHolder.getSurface());
            mediaRecorder.prepare();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mediaRecorder.start();
    }

    private void playVideo() {
        Uri uri1 = Uri.parse(filePath);
        MediaPlayer mediaPlayer = new MediaPlayer();
        if (mediaPlayer.isPlaying()){
            mediaPlayer.reset();
        }

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDisplay(mHolder);

        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri1);
            mediaPlayer.prepare();
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.open:
                openCamera();
                break;
            case R.id.recorder:
                recordVideo();
                break;
            case R.id.stop:
                mediaRecorder.stop();
                break;
            case R.id.play:
                playVideo();
                break;
        }
    }
}
