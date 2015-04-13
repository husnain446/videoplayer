package byteshaft.com.recorder;


import android.content.Context;
import android.content.ContextWrapper;
import android.media.MediaPlayer;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

public class Helpers extends ContextWrapper {

    public Helpers(Context base) {
        super(base);
    }

    int getInt(float input) {
        return Math.round(input);
    }

    int getDensityPixels(int pixels) {
        float dp = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, pixels, getResources().getDisplayMetrics());
        return getInt(dp);
    }

    boolean isVideoPortrait(MediaPlayer mp) {
        return mp.getVideoHeight() > mp.getVideoWidth();

    }

    int getHorizontalCenterOfView(View v) {
        return v.getWidth() / 2;
    }

    int getVerticalCenterOfView(View v) {
        return v.getHeight() / 2;
    }

    MediaPlayer getMediaPlayer() {
        return new MediaPlayer();
    }

    WindowManager getWindowManager() {
        return (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    }

    void togglePlayback(MediaPlayer mediaPlayer) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    void destroyVideoSurface(WindowManager mWindowManager, View view) {
        if (mWindowManager != null) {
            mWindowManager.removeView(view);
        }
    }
}
