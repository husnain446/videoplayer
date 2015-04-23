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
import java.io.FileOutputStream;
import java.io.IOException;
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

    ArrayList<String> getAllVideosUri() {
        ArrayList<String> uris = new ArrayList<>();
        Cursor cursor = getVideosCursor();
        int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        while (cursor.moveToNext()) {
            uris.add(cursor.getString(pathColumn));
        }
        cursor.close();
        return uris;
    }

    private Cursor getVideosCursor() {
        String[] Projection = {MediaStore.Video.Media._ID, MediaStore.Images.Media.DATA};
        return getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                Projection, null, null, null);
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

    void writeBitmapToFile(Bitmap bitmap, String fileName) {
        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 85, outputStream);
            }
            outputStream.flush();
            outputStream.close();
        } catch (IOException ignored) {
        }
    }
}
