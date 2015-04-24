package byteshaft.com.recorder;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
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
import android.widget.VideoView;

public class VideoOverlay extends Helpers implements SurfaceHolder.Callback,
        View.OnTouchListener, MediaPlayer.OnCompletionListener, View.OnClickListener {

    private final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
    private WindowManager mWindowManager;
    private Uri fileRepo;
    private int position;
    private WindowManager.LayoutParams params;
    private boolean clicked = false;
    private View mVideoOverlayLayout;
    private Button close;
    private VideoView videoView;
    private ScreenStateListener mScreenStateListener = null;
    private double initialX = 0;
    private double initialY = 0;
    private double initialTouchX = 0;
    private double initialTouchY = 0;


    public VideoOverlay(Context context) {
        super(context);
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

    void setVideoFile(Uri uri) {
        fileRepo = uri;
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
        videoView.setVideoURI(fileRepo);
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
        mWindowManager = getWindowManager();
        params = getCustomWindowManagerParameters();
        mWindowManager.addView(previewForCamera, params);
    }

    private WindowManager.LayoutParams getCustomWindowManagerParameters() {
        Bitmap mVideoMetadata = getMetadataForVideo(fileRepo.getPath());
        long height;
        long width;
        double ratio;
        if (isVideoPortrait(mVideoMetadata)) {
            width = getDensityPixels(150);
            ratio = getVideoHeight(mVideoMetadata) / getVideoWidth(mVideoMetadata);
            height = getInt(width * ratio);
        } else {
            height = getDensityPixels(150);
            ratio = getVideoWidth(mVideoMetadata) / getVideoHeight(mVideoMetadata);
            width = getInt(height * ratio);
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
                    if (videoView.isPlaying()) {
                        videoView.pause();
                        close.setVisibility(View.GONE);
                    } else {
                        videoView.start();
                        close.setVisibility(View.VISIBLE);
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
                videoView.stopPlayback();
                destroyVideoSurface(mWindowManager, mVideoOverlayLayout);
        }
    }
}
