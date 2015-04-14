package byteshaft.com.recorder;


import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

public class VideoOverlay extends ContextWrapper implements SurfaceHolder.Callback,
        View.OnTouchListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener {

    private WindowManager mWindowManager;
    private String fileRepo;
    private int position;
    private WindowManager.LayoutParams params;
    private MediaPlayer mediaPlayer;
    private boolean clicked = false;
    private View mVideoOverlayLayout;
    private Helpers mHelpers = null;

    public VideoOverlay(Context context) {
        super(context);
        mHelpers = new Helpers(getApplicationContext());
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mVideoOverlayLayout = inflater.inflate(R.layout.video_surface, null);
        SurfaceView surfaceView = (SurfaceView) mVideoOverlayLayout.findViewById(R.id.surface);

        SurfaceHolder holder = surfaceView.getHolder();
        holder.addCallback(this);
        surfaceView.setOnTouchListener(this);
    }

    void setVideoFile(String file) {
        fileRepo = file;
    }

    void setVideoStartPosition(int position) {

        this.position = position;
    }

    void startPlayback() {

        createSystemOverlayForPreview(mVideoOverlayLayout);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Uri uri1 = Uri.parse(fileRepo);
        mediaPlayer = mHelpers.getMediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mHelpers.prepareMediaPlayer(mediaPlayer, uri1, holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private void createSystemOverlayForPreview(View previewForCamera) {
        mWindowManager = mHelpers.getWindowManager();
        params = getCustomWindowManagerParameters();
        mWindowManager.addView(previewForCamera, params);
    }

    private WindowManager.LayoutParams getCustomWindowManagerParameters() {
        final int DESIRED_HEIGHT = mHelpers.getDensityPixels(100);
        final int DESIRED_WIDTH = (int) Math.round(DESIRED_HEIGHT * 1.77);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.height = DESIRED_HEIGHT;
        params.width = DESIRED_WIDTH;
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        params.format = PixelFormat.TRANSLUCENT;
        params.gravity = Gravity.TOP | Gravity.START;
        return params;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                clicked = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                int x = mHelpers.getHorizontalCenterOfView(v);
                int y = mHelpers.getVerticalCenterOfView(v);
                params.x = (int) (event.getRawX() - x);
                params.y = (int) (event.getRawY() - y);
                mWindowManager.updateViewLayout(mVideoOverlayLayout, params);
                clicked = false;
                return true;
            case MotionEvent.ACTION_UP:
                if (clicked) {
                    mHelpers.togglePlayback(mediaPlayer);
                }
                return true;
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mHelpers.destroyVideoSurface(mWindowManager, mVideoOverlayLayout);
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        float ratio;
        if (mHelpers.isVideoPortrait(mp)) {
            int ourWidth = mHelpers.getDensityPixels(100);
            ratio = mp.getVideoHeight() / mp.getVideoWidth();
            params.width = ourWidth;
            params.height = mHelpers.getInt(ourWidth * ratio);
        } else {
            int ourHeight = mHelpers.getDensityPixels(100);
            ratio = mp.getVideoWidth() / mp.getVideoHeight();
            params.height = ourHeight;
            params.width = mHelpers.getInt(ourHeight * ratio);
        }
        mp.seekTo(position);
        mp.start();
    }
}
