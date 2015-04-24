package byteshaft.com.recorder;


import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.VideoView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;


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

    void playVideoForLocation(String filename) {
        Intent intent = new Intent(getApplicationContext(), VideoPlayer.class);
        intent.putExtra("videoUri", filename);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    void deleteFile(int databaseIndex) {
        String[] projection = {MediaStore.Video.Media._ID};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        cursor.moveToPosition(databaseIndex);
        int id = cursor.getInt(idColumn);
        Uri uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
        getContentResolver().delete(uri, null, null);
    }

    int getDurationForVideo(int databaseIndex) {
        String[] projection = {MediaStore.Video.Media.DURATION};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION);
        cursor.moveToPosition(databaseIndex);
        String duration = cursor.getString(durationColumn);
        cursor.close();
        return Integer.valueOf(duration);
    }

    String getFormattedTime(int timeMs) {
        int totalSeconds = timeMs / 1000;

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        StringBuilder mFormatBuilder = new StringBuilder();
        Formatter mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

        mFormatBuilder.setLength(0);
        if (hours > 0) {
            return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else {
            return mFormatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    void setScreenBrightness(Window window, float value) {
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.screenBrightness = value;
        window.setAttributes(layoutParams);
    }

    void showLauncherHome() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }

    float getCurrentBrightness(Window window) {
        return window.getAttributes().screenBrightness;
    }

    int getCurrentVolume() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        return am.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    void setVolume(int level) {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, level, 0);
    }
}
