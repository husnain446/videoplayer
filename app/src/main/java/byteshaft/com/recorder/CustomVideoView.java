package byteshaft.com.recorder;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.VideoView;

import java.util.ArrayList;

public class CustomVideoView extends VideoView {

    static final int PLAYING = 1;
    static final int PAUSED = 0;

    private Uri currentlyPlayingVideoUri = null;
    private ArrayList<MediaPlayerStateChangedListener> mListeners = new ArrayList<>();

    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMediaPlayerStateChangedListener(MediaPlayerStateChangedListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void start() {
        super.start();
        for (MediaPlayerStateChangedListener listener : mListeners) {
            listener.onPlaybackStateChanged(1);
        }
    }

    @Override
    public void pause() {
        super.pause();
        setKeepScreenOn(false);
        for (MediaPlayerStateChangedListener listener : mListeners) {
            listener.onPlaybackStateChanged(0);
        }
    }

    @Override
    public void stopPlayback() {
        super.stopPlayback();
        currentlyPlayingVideoUri = null;
    }

    @Override
    public void setVideoURI(Uri uri) {
        super.setVideoURI(uri);
        currentlyPlayingVideoUri = uri;
    }

    public Uri getVideoURI() {
        return currentlyPlayingVideoUri;
    }

    public interface MediaPlayerStateChangedListener {
        public void onPlaybackStateChanged(int state);
    }
}
