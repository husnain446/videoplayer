package byteshaft.com.recorder;


import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.VideoView;


public class Helpers extends ContextWrapper {

    public Helpers(Context base) {
        super(base);
    }

    long getInt(double input) {
        return Math.round(input);
    }

    long getDensityPixels(int pixels) {
        float dp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, pixels, getResources().getDisplayMetrics());
        return getInt(dp);
    }

    boolean isVideoPortrait(Bitmap bitmap) {
        int videoHeight;
        int videoWidth;
        videoHeight = bitmap.getHeight();
        videoWidth = bitmap.getWidth();

        return videoHeight > videoWidth;
    }

    int getHorizontalCenterOfView(View v) {
        return v.getWidth() / 2;
    }

    int getVerticalCenterOfView(View v) {
        return v.getHeight() / 2;
    }

    WindowManager getWindowManager() {
        return (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    }

    void togglePlayback(VideoView videoView) {
        if (videoView.isPlaying()) {
            videoView.pause();
        } else {
            videoView.start();
        }
    }

    void destroyVideoSurface(WindowManager mWindowManager, View view) {
        if (mWindowManager != null) {
            mWindowManager.removeView(view);
        }
    }

    double getVideoHeight(Bitmap bitmap) {
        return (double) bitmap.getHeight();
    }

    double getVideoWidth(Bitmap bitmap) {
        return (double) bitmap.getWidth();
    }

    Bitmap getMetadataForVideo(String file) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(file);
        return retriever.getFrameAtTime();
    }
}
