package byteshaft.com.recorder;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.text.TextUtils;
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
    private ArrayList<Bitmap> thumbnails = null;
    private ArrayAdapter<String> modeAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Helpers mHelper = new Helpers(getApplicationContext());
        allVideos = mHelper.getAllVideosUri();
        realVideos = mHelper.getVideoTitles(allVideos);
        thumbnails = mHelper.getAllVideosThumbnails();
        modeAdapter = new ThumbnailAdapter(MainActivity.this, R.layout.row, realVideos);
        setListAdapter(modeAdapter);
        ColorDrawable white = new ColorDrawable(this.getResources().getColor(R.color.sage));
        getListView().setDivider(white);
        getListView().setDividerHeight(1);
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
            imageThumbnail.setImageBitmap(thumbnails.get(position));
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
}
