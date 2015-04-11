package byteshaft.com.recorder;


import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import java.io.IOException;

public class VideoOverlay extends ContextWrapper implements SurfaceHolder.Callback,
        View.OnTouchListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    SurfaceHolder mHolder;
    WindowManager mWindowManager;
    String fileRepo;
    int position;
    WindowManager.LayoutParams params;
    MediaPlayer mediaPlayer;
    boolean clicked = false;
    SurfaceView surfaceView;
    View view;

    public VideoOverlay(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.video_surface, null);
        surfaceView = (SurfaceView) view.findViewById(R.id.surface);

        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);
        surfaceView.setOnTouchListener(this);
    }

    void setVideoFile(String file) {
        fileRepo = file;
    }

    void setVideoStartPosition(int position) {
        this.position = position;
    }

    void startPlayback() {
        createSystemOverlayForPreview(view);
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        playVideo();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void createSystemOverlayForPreview(View previewForCamera) {
        mWindowManager = getWindowManager();
        params = getCustomWindowManagerParameters();
        mWindowManager.addView(previewForCamera, params);
    }

    private WindowManager getWindowManager() {
        return (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    }

    private WindowManager.LayoutParams getCustomWindowManagerParameters() {
        final int DESIRED_WIDTH = getDisplayPixels(150);
        final int DESIRED_HEIGHT = (int) Math.round(DESIRED_WIDTH * 1.77);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.height = DESIRED_HEIGHT;
        params.width = DESIRED_WIDTH;
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        params.format = PixelFormat.TRANSLUCENT;
        params.gravity = Gravity.TOP | Gravity.LEFT;
        return params;
    }

    private void playVideo() {
        Uri uri1 = Uri.parse(fileRepo);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
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
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                clicked = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                int x = v.getWidth() / 2;
                int y = v.getHeight() / 2;
                params.x = (int) (event.getRawX() - x);
                params.y = (int) (event.getRawY() - y );
                mWindowManager.updateViewLayout(view, params);
                clicked = false;
                return true;
            case MotionEvent.ACTION_UP:
                if (clicked) {
                    if (mediaPlayer.isPlaying()) {
                        mediaPlayer.pause();
                    } else {
                        mediaPlayer.start();
                    }
                }
                return true;
        }
        return false;
    }

    private int getDisplayPixels(int pixels) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, pixels, getResources().getDisplayMetrics()));
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        destroy();
    }

    private void destroy() {
        if (mWindowManager != null) {
            mWindowManager.removeView(view);
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        System.out.println(mp.getVideoHeight() + " " + mp.getVideoWidth());
        float ratio;
        if (mp.getVideoHeight() > mp.getVideoWidth()) {
            int ourWidth = getDisplayPixels(150);
            ratio = mp.getVideoHeight() / mp.getVideoWidth();


            params.width = ourWidth;
            params.height = Math.round(ourWidth * ratio);
        } else {
            int ourHeight = getDisplayPixels(150);
            ratio = mp.getVideoWidth() / mp.getVideoHeight();
            params.height = ourHeight;
            params.width = Math.round(ourHeight * ratio);
        }
        mWindowManager.updateViewLayout(view, params);
        mp.seekTo(position);
        mp.start();
    }
}
