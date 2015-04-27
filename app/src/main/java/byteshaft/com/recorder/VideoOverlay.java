package byteshaft.com.recorder;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class VideoOverlay extends RelativeLayout implements SurfaceHolder.Callback,
        MediaPlayer.OnCompletionListener, View.OnClickListener,
        CustomVideoView.MediaPlayerStateChangedListener {

    private final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
    private WindowManager mWindowManager;
    private Uri fileRepo;
    private int position;
    private WindowManager.LayoutParams params;
    private Button close;
    private ScreenStateListener mScreenStateListener = null;
    private double mVideoHeight = 0;
    private double mVideoWidth = 0;
    private boolean playOnStart;
    private Helpers mHelpers = null;
    private Context mContext = null;
    private GestureDetectorCompat mDetector = null;
    private CustomVideoView mCustomVideoView = null;

    public VideoOverlay(Context context) {
        super(context);
        mContext = context;
        mHelpers = new Helpers(mContext);
        mCustomVideoView = new CustomVideoView(mContext);
        close = getCloseButton();
        addView(mCustomVideoView);
        addView(close);
        mCustomVideoView.setOnCompletionListener(this);
        mCustomVideoView.setMediaPlayerStateChangedListener(this);
        mScreenStateListener = new ScreenStateListener(mCustomVideoView);
        SurfaceHolder holder = mCustomVideoView.getHolder();
        holder.addCallback(this);
        mDetector = new GestureDetectorCompat(mContext, new GestureListener());
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

    void setPlayOnStart(boolean start) {
        playOnStart = start;
    }

    void startPlayback() {
        createSystemOverlayForPreview(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mContext.registerReceiver(mScreenStateListener, filter);
        mCustomVideoView.setVideoURI(fileRepo);
        mCustomVideoView.seekTo(position);
        if (playOnStart) {
            mCustomVideoView.start();
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mContext.unregisterReceiver(mScreenStateListener);

    }

    private void createSystemOverlayForPreview(View previewForCamera) {
        mWindowManager = mHelpers.getWindowManager();
        params = getCustomWindowManagerParameters();
        mWindowManager.addView(previewForCamera, params);
    }

    private WindowManager.LayoutParams getCustomWindowManagerParameters() {
        double height;
        double width;
        double ratio;
        if (mHelpers.isVideoPortrait(mVideoHeight, mVideoWidth)) {
            width = mHelpers.getDensityPixels(150);
            ratio = mVideoHeight / mVideoWidth;
            height = width * ratio;
        } else {
            height = mHelpers.getDensityPixels(150);
            ratio = mVideoWidth / mVideoHeight;
            width = height * ratio;
        }
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.height = (int) height;
        params.width = (int) width;
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.format = PixelFormat.TRANSLUCENT;
        params.gravity = Gravity.TOP | Gravity.START;
        return params;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        this.mDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mHelpers.destroyVideoSurface(mWindowManager, this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bClose:
                mCustomVideoView.stopPlayback();
                mHelpers.destroyVideoSurface(mWindowManager, this);
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

    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private int initialX = 0;
        private int initialY = 0;
        private float initialTouchX = 0;
        private float initialTouchY = 0;

        @Override
        public boolean onDown(MotionEvent e) {
            initialX = params.x;
            initialY = params.y;
            initialTouchX = e.getRawX();
            initialTouchY = e.getRawY();
            return super.onDown(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            params.x = initialX + (int) (e2.getRawX() - initialTouchX);
            params.y = initialY + (int) (e2.getRawY() - initialTouchY);
            mWindowManager.updateViewLayout(VideoOverlay.this, params);
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            if (mCustomVideoView.isPlaying()) {
                mCustomVideoView.pause();
            } else {
                mCustomVideoView.start();
            }
            return super.onSingleTapUp(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            mCustomVideoView.pause();
            mHelpers.playVideoForLocation(fileRepo.getPath(), mCustomVideoView.getCurrentPosition());
            mHelpers.destroyVideoSurface(mWindowManager, VideoOverlay.this);
        }
    }

    private LinearLayout.LayoutParams getLayoutParametersForCloseButton() {
        int height = Math.round(mHelpers.getDensityPixels(40));
        int width = Math.round(mHelpers.getDensityPixels(40));
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, height);
        layoutParams.gravity = Gravity.TOP | Gravity.END;
        return layoutParams;
    }

    private Button getCloseButton() {
        Button button = new Button(mContext);
        button.setText("X");
        button.setLayoutParams(getLayoutParametersForCloseButton());
        button.setOnClickListener(this);
        button.setId(R.id.bClose);
        button.setVisibility(INVISIBLE);
        return button;
    }
}
