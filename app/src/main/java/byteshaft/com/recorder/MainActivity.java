package byteshaft.com.recorder;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity implements SearchView.OnQueryTextListener ,
        VideosListFragment.VideosListListener {

    private ArrayAdapter<String> mModeAdapter;
    private ArrayList<String> mVideosPathList;
    private String[] mVideosTitles;
    private CharSequence mDrawerTitle = "Video Player";
    private Helpers mHelper;
    private ListView mDrawerList;
    private String[] mListTitles = {"Videos", "Settings", "About"};
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private Fragment mFragment;
    private int mPositionGlobal;
    SearchView searchView;
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mHelper = new Helpers(getApplicationContext());
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = getActionBarDrawerToggle();
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mVideosPathList = mHelper.getAllVideosUri();
        mVideosTitles = mHelper.getVideoTitles(mVideosPathList);
        mModeAdapter = new VideoListAdapter(this, R.layout.row, mVideosPathList);
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

    @Override
    public void onVideoSelected(int position) {
        mHelper.playVideoForLocation(mVideosPathList.get(position), 0);
    }

    @Override
    public void onVideosListFragmentCreated() {
        VideosListFragment videosListFragment = (VideosListFragment) mFragment;
        videosListFragment.setListAdapter(mModeAdapter);
    }

    class VideoListAdapter extends ArrayAdapter<String> {

        public VideoListAdapter(Context context, int resource, ArrayList<String> videos) {
            super(context, resource, videos);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.row, parent, false);
                holder = new ViewHolder();
                holder.title = (TextView) convertView.findViewById(R.id.FilePath);
                holder.time = (TextView) convertView.findViewById(R.id.tv);
                holder.thumbnail = (ImageView) convertView.findViewById(R.id.Thumbnail);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.title.setText(mVideosTitles[position]);
            holder.time.setText(
                    mHelper.getFormattedTime(mHelper.getDurationForVideo(position)));
            holder.position = position;
            new ThumbnailCreationTask(getApplicationContext(), holder, position).execute();
            return convertView;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        SearchManager manager = (SearchManager) getSystemService(SEARCH_SERVICE);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchItem.getActionView();
        if (searchView != null) {
            searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));
            searchView.setOnQueryTextListener(this);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        mModeAdapter.getFilter().filter(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        videoSearchFilter(newText);
//        if (TextUtils.isEmpty(newText)) {
//            mModeAdapter.getFilter().filter("");
//        } else {
//            mModeAdapter.getFilter().filter(newText);
//        }
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
    public boolean onContextItemSelected (final MenuItem item){
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        String[] menuItems = {"Play", "Delete"};
        String menuItemName = menuItems[menuItemIndex];
        String listItemName = mVideosPathList.get(info.position);
        if (menuItemName.equals("Play")) {
            mHelper.playVideoForLocation(listItemName, 0);
        } else if (menuItemName.equals("Delete")) {
            showDeleteConfirmationDialog(info.position);
        }
        return super.onContextItemSelected(item);

    }

    @Override
    public void unregisterForContextMenu (@NonNull View view){
        super.unregisterForContextMenu(view);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            mPositionGlobal = position;
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        switch (position) {
            case 0:
                mFragment = new VideosListFragment();
                break;
            case 1:
                mFragment = new SettingFragment();
                break;
            case 2:
                mFragment = new AboutFragment();
                break;
        }

        // Insert the fragment by replacing any existing fragment
        android.app.FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.container, mFragment).commit();

        // Highlight the selected item, update the title, and close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(mListTitles[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    private void showDeleteConfirmationDialog(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Do you want to delete this video ?");
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mHelper.deleteFile(position);
                mModeAdapter.remove(mModeAdapter.getItem(position));
                mModeAdapter.notifyDataSetChanged();
            }
        });
        builder.create();
        builder.show();
    }

    private ActionBarDrawerToggle getActionBarDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open,
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
                getSupportActionBar().setTitle(mListTitles[mPositionGlobal]);
                invalidateOptionsMenu();
            }
        };
    }

    private void videoSearchFilter(String search) {
            if (search != null && search.length() > 0) {
                for (int i = 0; i < mVideosTitles.length; i++) {
                    if (!mVideosTitles[i].toLowerCase().contains(search)) {
                        System.out.println(mVideosTitles[i]);
                            mModeAdapter.remove(mModeAdapter.getItem(i));
                            mModeAdapter.notifyDataSetChanged();
                    }
                }
            }
    }

    static class ViewHolder {
        public TextView title;
        public TextView time;
        public ImageView thumbnail;
        public int position;
    }
}
