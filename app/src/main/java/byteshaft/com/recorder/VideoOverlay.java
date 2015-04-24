package byteshaft.com.recorder;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

public class VideoOverlay extends Helpers implements SurfaceHolder.Callback,
        View.OnTouchListener, MediaPlayer.OnCompletionListener, View.OnClickListener,
        CustomVideoView.MediaPlayerStateChangedListener {

    private final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
    private WindowManager mWindowManager;
    private Uri fileRepo;
    private int position;
    private WindowManager.LayoutParams params;
    private boolean clicked = false;
    private View mVideoOverlayLayout;
    private Button close;
    private CustomVideoView mCustomVideoView;
    private ScreenStateListener mScreenStateListener = null;
    private double initialX = 0;
    private double initialY = 0;
    private double initialTouchX = 0;
    private double initialTouchY = 0;
    private double mVideoHeight = 0;
    private double mVideoWidth = 0;

    public VideoOverlay(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mVideoOverlayLayout = inflater.inflate(R.layout.video_surface, null);
        mCustomVideoView = (CustomVideoView) mVideoOverlayLayout.findViewById(R.id.videoView);
        mCustomVideoView.setOnCompletionListener(this);
        mCustomVideoView.setOnTouchListener(this);
        mCustomVideoView.setMediaPlayerStateChangedListener(this);
        mScreenStateListener = new ScreenStateListener(mCustomVideoView);
        SurfaceHolder holder = mCustomVideoView.getHolder();
        holder.addCallback(this);
        close = (Button) mVideoOverlayLayout.findViewById(R.id.bClose);
        close.setOnClickListener(this);
    }

    void setVideoFile(Uri uri) {
        fileRepo = uri;
    }

    void setVideoStartPosition(int position) {
        this.position = position;
    }

    void setVideoHeight(int height) {
        mVideoHeight = height;
    }

    void setVideoWidth(int width) {
        mVideoWidth = width;
    }

    void startPlayback() {
        createSystemOverlayForPreview(mVideoOverlayLayout);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        registerReceiver(mScreenStateListener, filter);
        mCustomVideoView.setVideoURI(fileRepo);
        mCustomVideoView.seekTo(position);
        mCustomVideoView.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        unregisterReceiver(mScreenStateListener);

    }

    private void createSystemOverlayForPreview(View previewForCamera) {
        mWindowManager = getWindowManager();
        params = getCustomWindowManagerParameters();
        mWindowManager.addView(previewForCamera, params);
    }

    private WindowManager.LayoutParams getCustomWindowManagerParameters() {
        double height;
        double width;
        double ratio;
        if (isVideoPortrait(mVideoHeight, mVideoWidth)) {
            width = getDensityPixels(150);
            ratio = mVideoHeight / mVideoWidth;
            height = width * ratio;
        } else {
            height = getDensityPixels(150);
            ratio = mVideoWidth / mVideoHeight;
            width = height * ratio;
        }
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.height = (int) height;
        params.width = (int) width;
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.flags = WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        params.format = PixelFormat.RGBA_8888;
        params.gravity = Gravity.TOP | Gravity.START;
        return params;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialTouchX = event.getRawX();
                initialTouchY = event.getRawY();
                initialX = params.x;
                initialY = params.y;
                clicked = true;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (event.getRawX() > initialX + 10 || event.getRawX() < initialX - 10 ||
                        event.getRawY() > initialY + 10 || event.getRawY() < initialY - 10) {
                    params.x = (int) initialX + (int) (event.getRawX() - initialTouchX);
                    params.y = (int) initialY + (int) (event.getRawY() - initialTouchY);
                    mWindowManager.updateViewLayout(mVideoOverlayLayout, params);
                    clicked = false;
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (clicked) {
                    if (mCustomVideoView.isPlaying()) {
                        mCustomVideoView.pause();
                    } else {
                        mCustomVideoView.start();
                    }
                }
                return true;
        }
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        destroyVideoSurface(mWindowManager, mVideoOverlayLayout);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bClose:
                mCustomVideoView.stopPlayback();
                destroyVideoSurface(mWindowManager, mVideoOverlayLayout);
        }
    }

    @Override
    public void onPlaybackStateChanged(int state) {
        switch (state) {
            case CustomVideoView.PLAYING:
                close.setVisibility(View.GONE);
                break;
            case CustomVideoView.PAUSED:
                close.setVisibility(View.VISIBLE);
                break;
        }
    }
}
