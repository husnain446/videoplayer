package byteshaft.com.recorder;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

public class ThumbnailCreationTask extends AsyncTask<Void, Void, Bitmap> {

    private ImageView thumbnailContainer = null;
    private int thumbId = 0;
    private Context mContext = null;
    private Helpers mHelpers = null;
    private ArrayList<String> allVideos = null;

    public ThumbnailCreationTask(Context context, ImageView imageView,
                                 ArrayList<String> videoPaths, int position) {
        mContext = context;
        thumbnailContainer = imageView;
        thumbId = position;
        allVideos = videoPaths;
        mHelpers = new Helpers(mContext.getApplicationContext());
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        String[] projection = {MediaStore.Video.Media._ID};
        Cursor cursor = mContext.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        cursor.moveToPosition(thumbId);
        int id = cursor.getInt(idColumn);
        cursor.close();
        return MediaStore.Video.Thumbnails.getThumbnail(
                mContext.getContentResolver(), id, MediaStore.Video.Thumbnails.MICRO_KIND, null);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        File file = new File(allVideos.get(thumbId));
        String name = String.valueOf(file.hashCode());
        mHelpers.writeBitmapToFile(bitmap, name);
        String filePath = mHelpers.getFilesDir().getAbsolutePath() + "/" + name;
        Uri uri = Uri.parse(filePath);
        thumbnailContainer.setImageURI(uri);
    }
}