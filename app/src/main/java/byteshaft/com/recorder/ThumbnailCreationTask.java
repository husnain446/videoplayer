package byteshaft.com.recorder;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.provider.MediaStore;

public class ThumbnailCreationTask extends AsyncTask<Void, Void, Bitmap> {

    private Context mContext;
    private int mPosition;
    private MainActivity.ViewHolder mHolder;
    private Helpers mHelpers;

    public ThumbnailCreationTask(Context context, MainActivity.ViewHolder holder, int position) {
        mContext = context;
        mPosition = position;
        mHolder = holder;
        mHelpers = new Helpers(mContext.getApplicationContext());
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        String[] projection = {MediaStore.Video.Media._ID};
        Cursor cursor = mContext.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        cursor.moveToPosition(mPosition);
        int id = cursor.getInt(idColumn);
        cursor.close();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = BitmapCache.calculateInSampleSize(options,
                                                    (int) mHelpers.getDensityPixels(40),
                                                    (int) mHelpers.getDensityPixels(30));
        return MediaStore.Video.Thumbnails.getThumbnail(
                mContext.getContentResolver(), id, MediaStore.Video.Thumbnails.MICRO_KIND, options);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (mHolder.position == mPosition) {
            mHolder.thumbnail.setImageBitmap(bitmap);
            BitmapCache.addBitmapToMemoryCache(String.valueOf(mPosition), bitmap);
        }
    }
}
