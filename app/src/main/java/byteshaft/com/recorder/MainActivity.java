package byteshaft.com.recorder;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.net.Uri;
import android.app.ListFragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.ContextMenu;
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
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity implements SearchView.OnQueryTextListener {

    static ArrayAdapter<String> sModeAdapter = null;

    private ArrayList<String> mVideosPathList = null;
    private String[] mVideosTitles = null;
    private CharSequence mTitle = "Videos";
    private CharSequence mDrawerTitle = "Video Player";
    private Helpers mHelper = null;
    private ListView mDrawerList = null;
    private String[] mListTitles = {"Videos", "Settings", "About"};
    private DrawerLayout mDrawerLayout = null;
    private ActionBarDrawerToggle mDrawerToggle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mHelper = new Helpers(getApplicationContext());
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open,
                R.string.drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(mDrawerTitle);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getSupportActionBar().setTitle(mTitle);
                invalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mVideosPathList = mHelper.getAllVideosUri();
        mVideosTitles = mHelper.getVideoTitles(mVideosPathList);
        sModeAdapter = new VideoListAdapter(this, R.layout.row, mVideosTitles);
        mVideosPathList = mHelper.getAllVideosUri();
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<>(this,
                R.layout.drawer_list_item, mListTitles));
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        selectItem(0);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    class VideoListAdapter extends ArrayAdapter<String> {

        public VideoListAdapter(Context context, int resource, String[] videos) {
            super(context, resource, videos);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row, parent, false);
            }
            TextView textFilePath = (TextView) row.findViewById(R.id.FilePath);
            textFilePath.setText(mVideosTitles[position]);
            TextView textView = (TextView) row.findViewById(R.id.tv);
            textView.setText(mHelper.getFormattedTime(mHelper.getDurationForVideo(position)));
            ImageView imageThumbnail = (ImageView) row.findViewById(R.id.Thumbnail);
            File file = new File(mVideosPathList.get(position));
            String name = String.valueOf(file.hashCode());
            String filePath = getFilesDir().getAbsolutePath() + "/" + name;
            Uri uri = Uri.parse(filePath);
            File link = new File(uri.getPath());
            if (link.exists()) {
                imageThumbnail.setImageURI(uri);
            } else {
                new ThumbnailCreationTask(getApplicationContext(), imageThumbnail,
                        mVideosPathList, position).execute();
            }
            return row;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SearchManager manager = (SearchManager) getSystemService(SEARCH_SERVICE);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
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
            sModeAdapter.getFilter().filter("");
        } else {
            sModeAdapter.getFilter().filter(newText);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo
            menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(mVideosTitles[info.position]);
        String[] menuItems = {"Play", "Delete"};
        for (int i = 0; i < menuItems.length; i++) {
            menu.add(Menu.NONE, i, i, menuItems[i]);
        }
    }

    @Override
    public boolean onContextItemSelected (MenuItem item){
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        String[] menuItems = {"Play", "Delete"};
        String menuItemName = menuItems[menuItemIndex];
        String listItemName = mVideosPathList.get(info.position);
        if (menuItemName.equals("Play")) {
            mHelper.playVideoForLocation(listItemName);
        } else if (menuItemName.equals("Delete")) {
            mHelper.deleteFile(info.position);
            mVideosPathList.remove(info.position);

        }
        return super.onContextItemSelected(item);

    }

    @Override
    public void unregisterForContextMenu (View view){
        super.unregisterForContextMenu(view);
    }

    public class VideosFragment extends ListFragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            setListAdapter(sModeAdapter);
            return rootView;
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
            mHelper.playVideoForLocation(mVideosPathList.get(position));
        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        Fragment fragment = new VideosFragment();

        // Insert the fragment by replacing any existing fragment
        android.app.FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, fragment).commit();

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mListTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }
}
