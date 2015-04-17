package byteshaft.com.recorder;


import android.content.Context;
import android.content.ContextWrapper;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.VideoView;
import java.io.File;
import java.util.ArrayList;


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

    ArrayList<String> getAllVideos() {
        ArrayList<String> videosList = new ArrayList<>();
        String[] Projection = {MediaStore.Video.Media._ID, MediaStore.Images.Media.DATA};
        Cursor cursor =  getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                Projection, null, null, null);
        int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        while (cursor.moveToNext()) {
            videosList.add(cursor.getString(pathColumn));
        }
        cursor.close();
        return videosList;
    }

    String[] getVideoTitles(ArrayList<String> videos) {
        ArrayList<String> vids = new ArrayList<>();
        for (String video : videos) {
            File file = new File(video);
            vids.add(file.getName());
        }
        String[] realVideos = new String[vids.size()];
        return vids.toArray(realVideos);
    }


}
