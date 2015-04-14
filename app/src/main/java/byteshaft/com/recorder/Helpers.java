package byteshaft.com.recorder;


import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Helpers extends ContextWrapper {

    static boolean listDisplay = false;
    File folder = new File(Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp Video");

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

    void prepareMediaPlayer(MediaPlayer mp, Uri videoUri, SurfaceHolder holder) {
        if (mp.isPlaying()) {
            mp.reset();
        }
        mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mp.setDisplay(holder);
        try {
            mp.setDataSource(getApplicationContext(), videoUri);
            mp.prepare();
        } catch (IllegalStateException | IOException e) {
            e.printStackTrace();
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


}
