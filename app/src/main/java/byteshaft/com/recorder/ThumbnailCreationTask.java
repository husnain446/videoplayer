package byteshaft.com.recorder;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.provider.MediaStore;

public class ThumbnailCreationTask extends AsyncTask<Void, Void, Bitmap> {

    private Context mContext = null;
    private int mPosition;
    private MainActivity.ViewHolder mHolder;

    public ThumbnailCreationTask(Context context, MainActivity.ViewHolder holder, int position) {
        mContext = context;
        mPosition = position;
        mHolder = holder;
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
        return MediaStore.Video.Thumbnails.getThumbnail(
                mContext.getContentResolver(), id, MediaStore.Video.Thumbnails.MINI_KIND, null);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        if (mHolder.position == mPosition) {
            mHolder.thumbnail.setImageBitmap(bitmap);
        }
    }
}
