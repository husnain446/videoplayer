package byteshaft.com.recorder;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.ListFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
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
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements SearchView.OnQueryTextListener ,
        NavigationDrawerFragment.NavigationDrawerCallbacks {

    private  ArrayList<String> allVideos = null;
    private  String[] realVideos = null;
    static ArrayAdapter<String> modeAdapter = null;
    private CharSequence mTitle;
    NavigationDrawerFragment mNavigationDrawerFragment;
    private Helpers mHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHelper = new Helpers(getApplicationContext());
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        allVideos = mHelper.getAllVideosUri();
        realVideos = mHelper.getVideoTitles(allVideos);
        modeAdapter = new ThumbnailAdapter(this, R.layout.row, realVideos);
        allVideos = mHelper.getAllVideosUri();
//        registerForContextMenu(getListView());
    }

    private void playVideoForLocation(String filename) {
        Intent intent = new Intent(getApplicationContext(), VideoPlayer.class);
        intent.putExtra("videoUri", filename);
        startActivity(intent);
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
            TextView textFilePath = (TextView) row.findViewById(R.id.FilePath);
            textFilePath.setText(realVideos[position]);
            TextView textView = (TextView) row.findViewById(R.id.tv);
            textView.setText(stringForTime(getDurationForVideo(position)));
            ImageView imageThumbnail = (ImageView) row.findViewById(R.id.Thumbnail);
            File file = new File(allVideos.get(position));
            String name = String.valueOf(file.hashCode());
            String filePath = getFilesDir().getAbsolutePath() + "/" + name;
            Uri uri = Uri.parse(filePath);
            File link = new File(uri.getPath());
            if (link.exists()) {
                imageThumbnail.setImageURI(uri);
            } else {
                new ThumbnailCreationTask(imageThumbnail, position).execute();
            }
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
                        .replace(R.id.container, new Fragments().newInstance(position + 1, 0)).commit();
                break;
            case 1:
                FragmentManager fragmentManager1 = getSupportFragmentManager();
                fragmentManager1.beginTransaction()
                        .replace(R.id.container, new Fragments().newInstance(position + 1, 1)).commit();
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

    class ThumbnailCreationTask extends AsyncTask<Void, Void, Bitmap> {

        private ImageView thumbnailContainer = null;
        private int thumbId = 0;

        public ThumbnailCreationTask(ImageView imageView, int position) {
            thumbnailContainer = imageView;
            thumbId = position;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            String[] projection = {MediaStore.Video.Media._ID};
            Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    projection, null, null, null);
            int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            cursor.moveToPosition(thumbId);
            int id = cursor.getInt(idColumn);
                cursor.close();
            return MediaStore.Video.Thumbnails.getThumbnail(
                    getContentResolver(), id, MediaStore.Video.Thumbnails.MICRO_KIND, null);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            File file = new File(allVideos.get(thumbId));
            String name = String.valueOf(file.hashCode());
            mHelper.writeBitmapToFile(bitmap, name);
            String filePath = getFilesDir().getAbsolutePath() + "/" + name;
            Uri uri = Uri.parse(filePath);
            thumbnailContainer.setImageURI(uri);
        }
    }

    private String stringForTime(int timeMs) {
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
    @Override
    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo
            menuInfo){
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(realVideos[info.position]);
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
        String listItemName = allVideos.get(info.position);
        if (menuItemName.equals("Play")) {
            playVideoForLocation(listItemName);
        } else if (menuItemName.equals("Delete")) {
            deleteFile(info);
            allVideos.remove(info.position);

        }
        return super.onContextItemSelected(item);

    }

    @Override
    public void unregisterForContextMenu (View view){
        super.unregisterForContextMenu(view);
    }

    private void deleteFile(AdapterView.AdapterContextMenuInfo info) {
        String[] projection = {MediaStore.Video.Media._ID};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        cursor.moveToPosition(info.position);
        int id = cursor.getInt(idColumn);
        Uri uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id);
        getContentResolver().delete(uri, null, null);
    }

    private int getDurationForVideo(int position) {
        String[] projection = {MediaStore.Video.Media.DURATION};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION);
        cursor.moveToPosition(position);
        String duration = cursor.getString(durationColumn);
        cursor.close();
        return Integer.valueOf(duration);
    }
    public  class Fragments extends ListFragment {

        int fragmentValue;
        private static final String ARG_SECTION_NUMBER = "section_number";

        public Fragments newInstance(int sectionNumber , int value) {
            Fragments fragment = new Fragments();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            fragmentValue = value;
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = null;
            if (fragmentValue == 0) {
                rootView = inflater.inflate(R.layout.fragment_main, container, false);
                setListAdapter(MainActivity.modeAdapter);
            } else if (fragmentValue ==1) {
                rootView = inflater.inflate(R.layout.fragment, container, false);
            }
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((MainActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

        @Override
        public void onListItemClick(ListView l, View v, int position, long id) {
            super.onListItemClick(l, v, position, id);
            String videoName = allVideos.get(position);
            playVideoForLocation(videoName);
        }
    }
}
