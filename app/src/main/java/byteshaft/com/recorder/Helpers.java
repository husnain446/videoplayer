package byteshaft.com.recorder;


import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.VideoView;

import java.io.File;
import java.util.ArrayList;

public class Helpers extends ContextWrapper {

    static boolean listDisplay = false;
    File folder = new File(Environment.getExternalStorageDirectory()
            + "/DCIM/Camera/");

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

    ListView getListView() {
        ListView list = new ListView(this);
        listDisplay = true;
        ArrayList<String> videos = getVideosFileFromFolder();
        String[] realVideos = new String[videos.size()];
        realVideos = videos.toArray(realVideos);
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(
                getApplicationContext(), android.R.layout.simple_list_item_1, realVideos);
        list.setAdapter(modeAdapter);
        ColorDrawable white = new ColorDrawable(this.getResources().getColor(R.color.sage));
        list.setDivider(white);
        list.setDividerHeight(1);
        return list;
    }

    ArrayList<String> getVideosFileFromFolder() {
        ArrayList<String> data = new ArrayList<>();
        File[] getVideos = folder.listFiles();
        for (File file : getVideos) {
            data.add(file.getName());
        }
        return data;
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
