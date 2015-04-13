package byteshaft.com.recorder;


import android.content.Context;
import android.content.ContextWrapper;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.view.SurfaceHolder;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

@SuppressWarnings("deprecation")
public class Helpers extends ContextWrapper {

    MediaRecorder mediaRecorder;
    Camera mCamera;
    SurfaceHolder mHolder;
    File[] getVideos;
    ArrayAdapter<String> modeAdapter;
    String[] realVideos;
    static boolean listDisplay = false;
    ListView list;
    MediaPlayer mediaPlayer;

    public Helpers(Context base) {
        super(base);
    }

    void playVideo(Uri uri, SurfaceHolder holder) {
        mediaPlayer = new MediaPlayer();
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.reset();
        }

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDisplay(holder);

        try {
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.prepare();
        } catch (IllegalArgumentException | IllegalStateException | IOException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mediaPlayer.start();

    }

    ListView getListView() {
        list = new ListView(this);
        listDisplay = true;
        ArrayList<String> videos = getVideosFromFolder();
        realVideos = new String[videos.size()];
        realVideos = videos.toArray(realVideos);
        modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, realVideos);
        list.setAdapter(modeAdapter);
        return list;
    }

    ArrayList<String> getVideosFromFolder() {
        File folder = new File(Environment.getExternalStorageDirectory() + "/Recordings");
        ArrayList<String> data = new ArrayList<>();
        getVideos = folder.listFiles();
        for (File file : getVideos) {
            data.add(file.getName());
        }
        return data;
    }
}
