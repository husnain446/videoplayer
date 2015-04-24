package byteshaft.com.recorder;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.VideoView;

public class CustomVideoView extends VideoView {

    private Uri currentlyPlayingVideoUri = null;

    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void start() {
        super.start();
        setKeepScreenOn(true);
    }

    @Override
    public void pause() {
        super.pause();
        setKeepScreenOn(false);
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
}
