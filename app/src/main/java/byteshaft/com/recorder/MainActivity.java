package byteshaft.com.recorder;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivity extends ListActivity implements SearchView.OnQueryTextListener {

    private ArrayList<String> allVideos = null;
    private String[] realVideos = null;
    private ArrayAdapter<String> modeAdapter = null;
    private SparseArray<Bitmap> thumbnailArray = new SparseArray<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Helpers mHelper = new Helpers(getApplicationContext());
        allVideos = mHelper.getAllVideosUri();
        realVideos = mHelper.getVideoTitles(allVideos);
        modeAdapter = new ThumbnailAdapter(MainActivity.this, R.layout.row, realVideos);
        setListAdapter(modeAdapter);
        ColorDrawable color = new ColorDrawable(
                getResources().getColor(android.R.color.holo_blue_bright));
        getListView().setDivider(color);
        getListView().setDividerHeight(2);
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
            if (thumbnailArray.get(position, null) == null) {
                new ThumbnailCreationTask(imageThumbnail, position).execute(allVideos.get(position));
            } else {
                imageThumbnail.setImageBitmap(thumbnailArray.get(position));
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

    class ThumbnailCreationTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView thumbnailContainer = null;
        private int thumbId = 0;

        public ThumbnailCreationTask(ImageView imageView, int position) {
            thumbnailContainer = imageView;
            thumbId = position;
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return ThumbnailUtils.createVideoThumbnail(params[0],
                    MediaStore.Video.Thumbnails.MICRO_KIND);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            thumbnailArray.put(thumbId, bitmap);
            thumbnailContainer.setImageBitmap(bitmap);
        }
    }
}
