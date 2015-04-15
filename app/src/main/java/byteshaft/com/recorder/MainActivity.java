package byteshaft.com.recorder;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends ActionBarActivity implements ListView.OnItemClickListener {

    private ArrayList<String> allVideos = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        ListView list = getListView();
        relativeLayout.addView(list);
        list.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String videoName = allVideos.get(position);
        playVideoForLocation(videoName);
    }

    private void playVideoForLocation(String filename) {
        Intent intent = new Intent(getApplicationContext(), VideoPlayer.class);
        intent.putExtra("videoUri", filename);
        startActivity(intent);
    }

    ListView getListView() {
        allVideos = getAllVideos();
        String[] realVideos = getVideoTitles(allVideos);
        ListView list = new ListView(this);
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(
                getApplicationContext(), android.R.layout.simple_list_item_1, realVideos);
        list.setAdapter(modeAdapter);
        ColorDrawable white = new ColorDrawable(this.getResources().getColor(R.color.sage));
        list.setDivider(white);
        list.setDividerHeight(1);
        return list;
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
        return videosList;
    }

    private String[] getVideoTitles(ArrayList<String> videos) {
        ArrayList<String> vids = new ArrayList<>();
        for (String video : videos) {
            File file = new File(video);
            vids.add(file.getName());
        }
        String[] realVideos = new String[vids.size()];
        return vids.toArray(realVideos);
    }
}
