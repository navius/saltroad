package biz.navius.saltroad.defaultpoi;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import org.andnav.osm.util.GeoPoint;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import biz.navius.saltroad.R;
import biz.navius.saltroad.kml.DefaultPoiPoint;
import biz.navius.saltroad.kml.constants.PoiConstants;
import biz.navius.saltroad.utils.Ut;

public class DefaultPoiDetailActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.default_poi_detail);

        Bundle extras = getIntent().getExtras();
        if(extras == null) extras = new Bundle();
        final int id = extras.getInt("id", PoiConstants.EMPTY_ID);
        
        if (id >= 0) {
        	final DefaultPoiPoint focusedItem = getDefaultPoi(id);
        	
        	if (focusedItem != null){
    			final ImageView image = (ImageView) this.findViewById(R.id.image);
        		try{
        			String fileExtension = "png";
            		File defaultPoiImageFolder = Ut.getRMapsDefaultPoiImageDir(this);
            		String imgPath = defaultPoiImageFolder.getAbsolutePath() + File.separator + focusedItem.ImageName + "." + fileExtension;
    				FileInputStream fis = new FileInputStream(imgPath);
    				BufferedInputStream bis = new BufferedInputStream(fis);
    				Bitmap bitmapImage = BitmapFactory.decodeStream(bis);
        			image.setImageBitmap(bitmapImage);
        		} catch (Exception e) {
        			int imageID = R.drawable.no_image;
        			image.setImageResource(imageID);
        		}

     			final TextView title = (TextView) this.findViewById(R.id.name);
    			final TextView descr = (TextView) this.findViewById(R.id.comment);
    			
    			title.setText(focusedItem.Title);
    			descr.setText(focusedItem.Descr);
        	}
        }
	}

	public DefaultPoiPoint getDefaultPoi(int id){

		Ut.d("run getDefaultPoi");
		DefaultPoiPoint defaultPoiPoint = null;
		final File folder = Ut.getRMapsMainDir(this, "data");

		if (folder.canRead()) {
			SQLiteDatabase db = null;
			try {
				db = new biz.navius.saltroad.defaultpoi.DatabaseHelper(this, folder.getAbsolutePath() + "/defaultpoi.db").getReadableDatabase();
			} catch (Exception e) {
				db = null;
			}

			if(db != null){
				final String[] args = {Integer.toString(id)};
				Cursor c = db.rawQuery(PoiConstants.STAT_getDefaultPoi, args);
				if (c != null) {
					if (c.moveToFirst())
						defaultPoiPoint = new DefaultPoiPoint(c.getInt(4), c.getString(2), c.getString(3), new GeoPoint(
								(int) (1E6 * c.getDouble(0)), (int) (1E6 * c.getDouble(1))), c.getString(7), c.getString(8), c.getString(9));
					c.close();

				}
				db.close();
			}
		}

		Ut.d("getDefaultPoi finished");
		return defaultPoiPoint;
	}

}
