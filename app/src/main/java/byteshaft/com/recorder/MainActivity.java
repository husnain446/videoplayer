package byteshaft.com.recorder;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
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
import android.widget.SearchView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends ListActivity implements SearchView.OnQueryTextListener {

    private ArrayList<String> allVideos = null;
    private String[] realVideos = null;
    private ArrayAdapter<String> modeAdapter = null;
    private Helpers mHelper = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHelper = new Helpers(getApplicationContext());
        allVideos = mHelper.getAllVideosUri();
        setupListView();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        String videoName = allVideos.get(position);
        playVideoForLocation(videoName);
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
            TextView textfilePath = (TextView) row.findViewById(R.id.FilePath);
            textfilePath.setText(realVideos[position]);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
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
            getListView().clearTextFilter();
        } else {
            modeAdapter.getFilter().filter(newText);
        }
        return true;
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
        menu.setHeaderTitle(realVideos[info.position]);
        String[] menuItems = {"Play", "Delete"};
        for (int i = 0; i<menuItems.length; i++) {
            menu.add(Menu.NONE, i, i, menuItems[i]);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int menuItemIndex = item.getItemId();
        String[] menuItems = {"Play", "Delete"};
        String menuItemName = menuItems[menuItemIndex];
        String listItemName = allVideos.get(info.position);
        if (menuItemName.equals("Play")) {
            playVideoForLocation(listItemName);
        } else if (menuItemName.equals("Delete")) {
            deleteFile(info);
            allVideos.remove(info.position);
            setupListView();

        }
        return super.onContextItemSelected(item);

    }

    @Override
    public void unregisterForContextMenu(View view) {
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

    private void setupListView() {
        realVideos = mHelper.getVideoTitles(allVideos);
        modeAdapter = new ThumbnailAdapter(MainActivity.this, R.layout.row, realVideos);
        setListAdapter(modeAdapter);
        getListView().setDivider(null);
        registerForContextMenu(getListView());
    }
}
