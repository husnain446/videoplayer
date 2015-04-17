package byteshaft.com.recorder;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity implements ListView.OnItemClickListener, SearchView.OnQueryTextListener {

    private ArrayList<String> allVideos = null;
    ArrayAdapter<String> modeAdapter = null;
    ListView list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.mainLayout);
        list = getListView();
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
        Helpers mHelper = new Helpers(getApplicationContext());
        allVideos = mHelper.getAllVideos();
        String[] realVideos = mHelper.getVideoTitles(allVideos);
        ListView list = new ListView(this);
        modeAdapter = new ArrayAdapter<>(
                getApplicationContext(), android.R.layout.simple_list_item_1, realVideos);
        list.setAdapter(modeAdapter);
        ColorDrawable white = new ColorDrawable(this.getResources().getColor(R.color.sage));
        list.setDivider(white);
        list.setDividerHeight(1);
        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (searchView != null) {
            searchView.setOnQueryTextListener(this);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            modeAdapter.getFilter().filter("");
            list.clearTextFilter();
        } else {
            modeAdapter.getFilter().filter(newText);
        }
        return true;
    }
}
