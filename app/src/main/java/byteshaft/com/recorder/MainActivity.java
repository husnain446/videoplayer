package byteshaft.com.recorder;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends ActionBarActivity implements ListView.OnItemClickListener {

    private VideoOverlay overlay;
    RelativeLayout relativeLayout;
    String strDate;
    static String filePath;
    Helpers mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mHelper = new Helpers(this);
        allReferences();
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        strDate = sdf.format(c.getTime());
        if (!mHelper.folder.exists()) {
            mHelper.folder.mkdir();
        }
        overlay = new VideoOverlay(getApplicationContext());
        filePath = mHelper.folder + "/" + "Video" + ".mp4";

        ListView list = mHelper.getListView();
        relativeLayout.addView(list);
        if (Helpers.listDisplay = false) {
            relativeLayout.addView(list);

        } else if (Helpers.listDisplay = true) {
            Log.i("Video Recorder", "ListDisplayIsTrue");
        }
        list.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String videoName = parent.getItemAtPosition(position).toString();
        getSelectedFileFromStorage(videoName);
    }

    private void getSelectedFileFromStorage(String item) {
        File[] getVideos;
        getVideos = mHelper.folder.listFiles();
        for (File file : getVideos) {
            if (file.getName().equals(item)) {
                String filename = file.getAbsolutePath();
                Intent intent = new Intent(getApplicationContext(), VideoPlayer.class);
                intent.putExtra("videoUri", filename);
                startActivity(intent);
                break;
            }
        }
    }

    private void allReferences() {
        relativeLayout = (RelativeLayout) findViewById(R.id.mainLayout);
    }

//    private void startPlayback() {
//        overlay.setVideoFile(filePath);
//        overlay.setVideoStartPosition(seekPosition);
//        overlay.startPlayback();
//    }
}
