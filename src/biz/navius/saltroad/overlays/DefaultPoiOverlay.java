package biz.navius.saltroad.overlays;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;
import org.andnav.osm.views.util.constants.OpenStreetMapViewConstants;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import biz.navius.saltroad.R;
import biz.navius.saltroad.kml.DefaultPoiPoint;
import biz.navius.saltroad.kml.constants.PoiConstants;
import biz.navius.saltroad.utils.Ut;

public class DefaultPoiOverlay extends OpenStreetMapViewOverlay {
	private Context mCtx;
	//private PoiManager mPoiManager;
	private int mTapIndex;
	private GeoPoint mLastMapCenter;
	private int mLastZoom;
	private PoiListThread mThread;
	private RelativeLayout mT;
	private float mDensity;
	private boolean mNeedUpdateList = false;

	public int getTapIndex() {
		return mTapIndex;
	}

	public void setTapIndex(int mTapIndex) {
		this.mTapIndex = mTapIndex;
	}
	
	public void UpdateList() {
		mNeedUpdateList = true;
	}

	protected OnItemTapListener<DefaultPoiPoint> mOnItemTapListener;
	protected OnItemLongPressListener<DefaultPoiPoint> mOnItemLongPressListener;
	protected List<DefaultPoiPoint> mItemList;
	protected final Point mMarkerHotSpot;
	protected final int mMarkerWidth, mMarkerHeight;
	private boolean mCanUpdateList = true;
	protected HashMap<Integer, Drawable> mBtnMap;

	public DefaultPoiOverlay(Context ctx,
			OnItemTapListener<DefaultPoiPoint> onItemTapListener, boolean hidepoi)
	{
		mCtx = ctx;
		//mPoiManager = poiManager;
		mCanUpdateList = !hidepoi;
		mTapIndex = -1;

		Drawable marker = ctx.getResources().getDrawable(R.drawable.default_poi_icon);
		this.mMarkerWidth = marker.getIntrinsicWidth();
		this.mMarkerHeight = marker.getIntrinsicHeight();

		mBtnMap = new HashMap<Integer, Drawable>();
		mBtnMap.put(new Integer(R.drawable.default_poi_icon), marker);
		this.mMarkerHotSpot = new Point(0, mMarkerHeight);

        this.mOnItemTapListener = onItemTapListener;

        mLastMapCenter = null;
        mLastZoom = -1;
        mThread = new PoiListThread();

		this.mT = (RelativeLayout) LayoutInflater.from(ctx).inflate(R.layout.default_poi, null);
		this.mT.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		DisplayMetrics metrics = new DisplayMetrics();
		((Activity) ctx).getWindowManager().getDefaultDisplay().getMetrics(metrics);
		mDensity = metrics.density;
	}

	@Override
	public void onDraw(Canvas c, OpenStreetMapView mapView) {
		final OpenStreetMapViewProjection pj = mapView.getProjection();
		final Point curScreenCoords = new Point();

		if (mCanUpdateList){
			boolean looseCenter = false;
			GeoPoint center = mapView.getMapCenter();
			GeoPoint lefttop = pj.fromPixels(0, 0);
			double deltaX = Math.abs(center.getLongitude() - lefttop.getLongitude());
			double deltaY = Math.abs(center.getLatitude() - lefttop.getLatitude());

			if (mLastMapCenter == null || mLastZoom != mapView.getZoomLevel())
				looseCenter = true;
			else if(0.7 * deltaX < Math.abs(center.getLongitude() - mLastMapCenter.getLongitude()) || 0.7 * deltaY < Math.abs(center.getLatitude() - mLastMapCenter.getLatitude()))
				looseCenter = true;

			if(looseCenter || mNeedUpdateList){
				mLastMapCenter = center;
				mLastZoom = mapView.getZoomLevel();
				mNeedUpdateList = false;

				mThread.setParams(1.5*deltaX, 1.5*deltaY);
				mThread.run();
			}
		}

		if (this.mItemList != null) {

			/*
			 * Draw in backward cycle, so the items with the least index are on
			 * the front.
			 */
			for (int i = this.mItemList.size() - 1; i >= 0; i--) {
				if (i != mTapIndex) {
					DefaultPoiPoint item = this.mItemList.get(i);
					pj.toPixels(item.GeoPoint, curScreenCoords);

					c.save();
					c.rotate(mapView.getBearing(), curScreenCoords.x,
							curScreenCoords.y);

					onDrawItem(c, i, curScreenCoords);

					c.restore();
				}
			}

			if (mTapIndex >= 0 && mTapIndex < this.mItemList.size()) {
				DefaultPoiPoint item = this.mItemList.get(mTapIndex);
				pj.toPixels(item.GeoPoint, curScreenCoords);

				c.save();
				c.rotate(mapView.getBearing(), curScreenCoords.x,
						curScreenCoords.y);

				onDrawItem(c, mTapIndex, curScreenCoords);

				c.restore();
			}
		}
	}

	protected void onDrawItem(Canvas c, int index, Point screenCoords) {
		final DefaultPoiPoint focusedItem = mItemList.get(index);
		int iconID;
		int imageID;

		try{
			iconID = mCtx.getResources().getIdentifier(focusedItem.IconName, "drawable", mCtx.getPackageName());
		} catch (Exception e) {
			iconID = R.drawable.default_poi_icon;
		}
		
		try{
			imageID = mCtx.getResources().getIdentifier(focusedItem.ImageName, "drawable", mCtx.getPackageName());
		} catch (Exception e) {
			imageID = R.drawable.no_image;
		}

		if (index == mTapIndex) {
			final ImageView pic = (ImageView) mT.findViewById(R.id.pic);
			final TextView title = (TextView) mT.findViewById(R.id.poi_title);
			final TextView descr = (TextView) mT.findViewById(R.id.descr);
			final TextView coord = (TextView) mT.findViewById(R.id.coord);
			final ImageView image = (ImageView) mT.findViewById(R.id.image);
			
			pic.setImageResource(iconID);
			title.setText(focusedItem.Title);
			descr.setText(focusedItem.Descr);
			coord.setText(Ut.formatGeoPoint(focusedItem.GeoPoint));
			image.setImageResource(imageID);

			mT.measure(0, 0);
			mT.layout(0, 0, mT.getMeasuredWidth(), mT.getMeasuredHeight());
			
			c.save();
			c.translate(screenCoords.x, screenCoords.y - pic.getMeasuredHeight() - pic.getTop());
			mT.draw(c);
			c.restore();
			
		} else {

			final int left = screenCoords.x - this.mMarkerHotSpot.x;
			final int right = left + this.mMarkerWidth;
			final int top = screenCoords.y - this.mMarkerHotSpot.y;
			final int bottom = top + this.mMarkerHeight;
	
			Integer key = new Integer(iconID);
			Drawable marker = null;
			if(mBtnMap.containsKey(key))
				marker = mBtnMap.get(key);
			else {
				try{
					marker = mCtx.getResources().getDrawable(iconID);
				} catch (Exception e) {
					marker = mCtx.getResources().getDrawable(R.drawable.default_poi_icon);
				}
				mBtnMap.put(key, marker);
			}
	
			marker.setBounds(left, top, right, bottom);
	
			marker.draw(c);
	
			if(OpenStreetMapViewConstants.DEBUGMODE){
				final int pxUp = 2;
				final int left2 = (int)(screenCoords.x + mDensity*(5 - pxUp));
				final int right2 = (int)(screenCoords.x + mDensity*(38 + pxUp));
				final int top2 = (int)(screenCoords.y - this.mMarkerHotSpot.y - mDensity*(pxUp));
				final int bottom2 = (int)(top2 + mDensity*(33 + pxUp));
				Paint p = new Paint();
				c.drawLine(left2, top2, right2, bottom2, p);
				c.drawLine(right2, top2, left2, bottom2, p);
				
				c.drawLine(screenCoords.x - 5, screenCoords.y - 5, screenCoords.x + 5, screenCoords.y + 5, p);
				c.drawLine(screenCoords.x - 5, screenCoords.y + 5, screenCoords.x + 5, screenCoords.y - 5, p);
			}
		}
	}

	public DefaultPoiPoint getDefaultPoiPoint(final int index){
		return this.mItemList.get(index);
	}

	public int getMarkerAtPoint(final int eventX, final int eventY, OpenStreetMapView mapView){
		if(this.mItemList != null){
			final OpenStreetMapViewProjection pj = mapView.getProjection();

			final Rect curMarkerBounds = new Rect();
			final Point mCurScreenCoords = new Point();

			 
			for(int i = 0; i < this.mItemList.size(); i++){
				final DefaultPoiPoint mItem = this.mItemList.get(i);
				pj.toPixels(mItem.GeoPoint, mapView.getBearing(), mCurScreenCoords);

				final int pxUp = 2;
				final int left = (int)(mCurScreenCoords.x + mDensity*(5 - pxUp));
				final int right = (int)(mCurScreenCoords.x + mDensity*(38 + pxUp));
				final int top = (int)(mCurScreenCoords.y - this.mMarkerHotSpot.y - mDensity*(pxUp));
				final int bottom = (int)(top + mDensity*(33 + pxUp));

				curMarkerBounds.set(left, top, right, bottom);
				if(curMarkerBounds.contains(eventX, eventY))
					return i;
			}
		}

		return -1;
	}

	@Override
	public boolean onSingleTapUp(MotionEvent event, OpenStreetMapView mapView) {
		final int index = getMarkerAtPoint((int)event.getX(), (int)event.getY(), mapView);
		if (index >= 0)
			if (onTap(index))
				return true;

		return super.onSingleTapUp(event, mapView);
	}

	@Override
	public boolean onLongPress(MotionEvent event, OpenStreetMapView mapView) {
		final int index = getMarkerAtPoint((int)event.getX(), (int)event.getY(), mapView);
		if (index >= 0)
			if (onLongLongPress(index))
				return true;

		return super.onLongPress(event, mapView);
	}

	private boolean onLongLongPress(int index) {
		return false;
//		if(this.mOnItemLongPressListener != null)
//			return this.mOnItemLongPressListener.onItemLongPress(index, this.mItemList.get(index));
//		else
//			return false;
	}

	protected boolean onTap(int index) {
		if(mTapIndex == index)
			mTapIndex = -1;
		else
			mTapIndex = index;

		if(this.mOnItemTapListener != null)
			return this.mOnItemTapListener.onItemTap(index, this.mItemList.get(index));
		else
			return false;
	}

	@SuppressWarnings("hiding")
	public static interface OnItemTapListener<DefaultPoiPoint>{
		public boolean onItemTap(final int aIndex, final DefaultPoiPoint aItem);
	}

	@SuppressWarnings("hiding")
	public static interface OnItemLongPressListener<DefaultPoiPoint>{
		public boolean onItemLongPress(final int aIndex, final DefaultPoiPoint aItem);
	}

	@Override
	protected void onDrawFinished(Canvas c, OpenStreetMapView osmv) {
	}



	private class PoiListThread extends Thread {
		private double mdeltaX;
		private double mdeltaY;

		public void setParams(double deltaX, double deltaY){
			mdeltaX = deltaX;
			mdeltaY = deltaY;
		}

		@Override
		public void run() {
			//mItemList = mPoiManager.getPoiListNotHidden(mLastZoom, mLastMapCenter, mdeltaX, mdeltaY);
			Ut.d("run Default PoiListThread");

			final File folder = Ut.getRMapsMainDir(mCtx, "data");
			if(folder.canRead()){
				SQLiteDatabase db = null;
				try {
					db = new biz.navius.saltroad.defaultpoi.DatabaseHelper(mCtx, folder.getAbsolutePath() + "/defaultpoi.db").getReadableDatabase();
				} catch (Exception e) {
					db = null;
				}

				Double left = mLastMapCenter.getLongitude() - mdeltaX;
				Double right = mLastMapCenter.getLongitude() + mdeltaX;
				Double bottom = mLastMapCenter.getLatitude() + mdeltaY;
				Double top = mLastMapCenter.getLatitude() - mdeltaY;
				
				// TODO D.Adachi
				String lang = "ja";
				
				final String[] args = {lang, Double.toString(left), Double.toString(right), Double.toString(top), Double.toString(bottom)};

				if(db != null){
					mItemList = doCreatePoiListFromCursor(db.rawQuery(PoiConstants.STAT_GET_DEFAULTPOI_LIST, args));
					db.close();
				};
			};

			Ut.d("Default PoiList maped");

			super.run();
		}
			

	}

	private List<DefaultPoiPoint> doCreatePoiListFromCursor(Cursor c){
		final ArrayList<DefaultPoiPoint> items = new ArrayList<DefaultPoiPoint>();
		if (c != null) {
			if (c.moveToFirst()) {
				do {
					items.add(new DefaultPoiPoint(c.getInt(4), c.getString(2), c.getString(3), new GeoPoint(
							(int) (1E6 * c.getDouble(0)), (int) (1E6 * c.getDouble(1))), c.getString(7), c.getString(8), c.getString(9)));
				} while (c.moveToNext());
			}
			c.close();
		}

		return items;
	}

}


