package byteshaft.com.recorder;

import android.content.ContentUris;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

public class Helpers extends ContextWrapper {

    public Helpers(Context base) {
        super(base);
    }

    float getDensityPixels(int pixels) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, pixels, getResources().getDisplayMetrics());
    }

    boolean isVideoPortrait(double height, double width) {
        return height > width;
    }

    WindowManager getWindowManager() {
        return (WindowManager) getSystemService(Context.WINDOW_SERVICE);
    }

    void destroyVideoSurface(WindowManager mWindowManager, View view) {
        if (mWindowManager != null) {
            mWindowManager.removeView(view);
        }
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

    void playVideoForLocation(String filename, int startPosition) {
        Intent intent = new Intent(getApplicationContext(), VideoPlayer.class);
        intent.putExtra("videoUri", filename);
        intent.putExtra("startPosition", startPosition);
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
        cursor.close();
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

    String getResolutionForVideo(int databaseIndex) {
        String[] projection = {MediaStore.Video.Media.RESOLUTION};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        int resolutionColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.RESOLUTION);
        cursor.moveToPosition(databaseIndex);
        String resolution = cursor.getString(resolutionColumn);
        cursor.close();
        return resolution;
    }

    String getCreationDate(int databaseIndex) {
        String[] projection = {MediaStore.Video.Media.DATE_TAKEN};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATE_TAKEN);
        cursor.moveToPosition(databaseIndex);
        String dateTaken = cursor.getString(durationColumn);
        String CreationDate = getDate(dateTaken , "dd-MM-yyy");
        cursor.close();
        return CreationDate;
    }

    String getVideoAlbumName(int databaseIndex) {
        String[] projection = {MediaStore.Video.Media.ALBUM};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.ALBUM);
        cursor.moveToPosition(databaseIndex);
        String dateTaken = cursor.getString(albumColumn);
        cursor.close();
        return dateTaken;
    }

    String getArtist(int databaseIndex) {
        String[] projection = {MediaStore.Video.Media.ARTIST};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.ARTIST);
        cursor.moveToPosition(databaseIndex);
        String dateTaken = cursor.getString(artistColumn);
        cursor.close();
        return dateTaken;
    }

    String getVideoTitle(int databaseIndex) {
        String[] projection = {MediaStore.Video.Media.TITLE};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.TITLE);
        cursor.moveToPosition(databaseIndex);
        String title = cursor.getString(titleColumn);
        cursor.close();
        return title;
    }

    String getLocation(int databaseIndex) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        int dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA);
        cursor.moveToPosition(databaseIndex);
        String dateTaken = cursor.getString(dataColumn);
        cursor.close();
        return dateTaken;
    }

    String getVideoCategory(int databaseIndex) {
        String[] projection = {MediaStore.Video.Media.CATEGORY};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        int categoryColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.CATEGORY);
        cursor.moveToPosition(databaseIndex);
        String dateTaken = cursor.getString(categoryColumn);
        cursor.close();
        return dateTaken;
    }

    public static String getDate(String milliSeconds, String dateFormat)
    {
        // Create a DateFormatter object for displaying date in specified format.
        SimpleDateFormat formatter = new SimpleDateFormat(dateFormat);

        // Create a calendar object that will convert the date and time value in milliseconds to date.
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(milliSeconds));
        return formatter.format(calendar.getTime());
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
