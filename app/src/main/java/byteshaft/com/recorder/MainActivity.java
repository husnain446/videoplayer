package byteshaft.com.recorder;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements SearchView.OnQueryTextListener ,
        NavigationDrawerFragment.NavigationDrawerCallbacks , ListView.OnItemClickListener {

    private  ArrayList<String> allVideos = null;
    private  String[] realVideos = null;
    private  ArrayList<Bitmap> thumbnails = null;
    static ArrayAdapter<String> modeAdapter = null;
    private CharSequence mTitle;
    NavigationDrawerFragment mNavigationDrawerFragment;
    RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        relativeLayout = (RelativeLayout) findViewById(R.id.videoLayout);
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        Helpers mHelper = new Helpers(this);
        allVideos = mHelper.getAllVideosUri();
        realVideos = mHelper.getVideoTitles(allVideos);
        thumbnails = mHelper.getAllVideosThumbnails();
        modeAdapter = new ThumbnailAdapter(this, R.layout.row, realVideos);
    }
    private void playVideoForLocation(String filename) {
        Intent intent = new Intent(getApplicationContext(), VideoPlayer.class);
        intent.putExtra("videoUri", filename);
        startActivity(intent);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String videoName = allVideos.get(position);
        playVideoForLocation(videoName);
    }

    class ThumbnailAdapter extends ArrayAdapter<String> {

        public ThumbnailAdapter(Context context, int resource, String[] videos) {
            super(context, resource, videos);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row, parent, false);
            }
            TextView textfilePath = (TextView) row.findViewById(R.id.FilePath);
            textfilePath.setText(realVideos[position]);
            ImageView imageThumbnail = (ImageView) row.findViewById(R.id.Thumbnail);
            imageThumbnail.setImageBitmap(thumbnails.get(position));
            return row;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        SearchManager manager = (SearchManager) getSystemService(SEARCH_SERVICE);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        if (searchView != null) {
            searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(this);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            modeAdapter.notifyDataSetChanged();
            modeAdapter.getFilter().filter("");
        } else {
            modeAdapter.getFilter().filter(newText);
        }
        return true;
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        switch (position) {
            case 0:
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, Fragments.newInstance(position + 1, 0)).commit();
                break;
            case 1:
                FragmentManager fragmentManager1 = getSupportFragmentManager();
                fragmentManager1.beginTransaction()
                        .replace(R.id.container, Fragments.newInstance(position + 1, 1)).commit();
                break;
        }
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
