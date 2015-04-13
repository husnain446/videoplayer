package byteshaft.com.recorder;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class VideoPlayer extends Activity implements SurfaceHolder.Callback {

    private SurfaceView mSurfaceView = null;
    Helpers mHelper = null;
    private Uri uri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        Bundle bundle = getIntent().getExtras();
        String videoPath = bundle.getString("videoUri");
        uri = Uri.parse(videoPath);
        mSurfaceView = (SurfaceView) findViewById(R.id.display);
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mHelper = new Helpers(getApplicationContext());
        mHelper.playVideo(uri, holder);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
