package byteshaft.com.recorder;


import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.VideoView;

public class VideoOverlay extends ContextWrapper implements SurfaceHolder.Callback,
        View.OnTouchListener, MediaPlayer.OnCompletionListener, View.OnClickListener {

    private final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
    private WindowManager mWindowManager;
    private String fileRepo;
    private int position;
    private WindowManager.LayoutParams params;
    private boolean clicked = false;
    private View mVideoOverlayLayout;
    private Helpers mHelpers = null;
    private Button close;
    private VideoView videoView;
    private ScreenStateListener mScreenStateListener = null;


    public VideoOverlay(Context context) {
        super(context);
        mHelpers = new Helpers(getApplicationContext());
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mVideoOverlayLayout = inflater.inflate(R.layout.video_surface, null);
        videoView = (VideoView) mVideoOverlayLayout.findViewById(R.id.videoView);
        videoView.setOnCompletionListener(this);
        videoView.setOnTouchListener(this);
        mScreenStateListener = new ScreenStateListener(videoView);
        SurfaceHolder holder = videoView.getHolder();
        holder.addCallback(this);
        close = (Button) mVideoOverlayLayout.findViewById(R.id.bClose);
        close.setOnClickListener(this);
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
        registerReceiver(mScreenStateListener, filter);
        videoView.setVideoPath(fileRepo);
        videoView.seekTo(position);
        videoView.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        unregisterReceiver(mScreenStateListener);

    }

    private void createSystemOverlayForPreview(View previewForCamera) {
        mWindowManager = mHelpers.getWindowManager();
        params = getCustomWindowManagerParameters();
        mWindowManager.addView(previewForCamera, params);
    }

    private WindowManager.LayoutParams getCustomWindowManagerParameters() {
        Bitmap mVideoMetadata = mHelpers.getMetadataForVideo(fileRepo);
        long height;
        long width;
        double ratio;
        if (mHelpers.isVideoPortrait(mVideoMetadata)) {
            width = mHelpers.getDensityPixels(150);
            ratio = mHelpers.getVideoHeight(mVideoMetadata) / mHelpers.getVideoWidth(mVideoMetadata);
            height = mHelpers.getInt(width * ratio);
        } else {
            height = mHelpers.getDensityPixels(150);
            ratio = mHelpers.getVideoWidth(mVideoMetadata) / mHelpers.getVideoHeight(mVideoMetadata);
            width = mHelpers.getInt(height * ratio);
        }
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.height = (int) height;
        params.width = (int) width;
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
                    mHelpers.togglePlayback(videoView);
                    toggleCloseButtonVisibility();
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bClose:
                videoView.stopPlayback();
                mHelpers.destroyVideoSurface(mWindowManager, mVideoOverlayLayout);
        }
    }

    private void toggleCloseButtonVisibility() {
        if (videoView.isPlaying()) {
            close.setVisibility(View.INVISIBLE);
        } else {
            close.setVisibility(View.VISIBLE);
        }
    }
}
