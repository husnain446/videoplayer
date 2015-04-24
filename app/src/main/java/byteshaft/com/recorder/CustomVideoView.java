package byteshaft.com.recorder;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

public class CustomVideoView extends VideoView {

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

    
}
