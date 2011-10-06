package biz.navius.saltroad.overlays;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.andnav.osm.util.GeoPoint;
import org.andnav.osm.views.OpenStreetMapView;
import org.andnav.osm.views.OpenStreetMapView.OpenStreetMapViewProjection;
import org.andnav.osm.views.overlay.OpenStreetMapViewOverlay;
import org.andnav.osm.views.util.OpenStreetMapTileFilesystemProvider;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.os.Handler;
import android.os.Message;

import biz.navius.saltroad.MainMapActivity;
import biz.navius.saltroad.R;
import biz.navius.saltroad.constants.MapConstants;
import biz.navius.saltroad.kml.PoiManager;
import biz.navius.saltroad.kml.Track;
import biz.navius.saltroad.utils.Ut;

public class DefaultTrackOverlay extends OpenStreetMapViewOverlay {
	private Paint mPaint;
	private int mLastZoom;
	private List<Path> mPaths;
	private List<Track> mTracks;
	private Point mBaseCoords;
	private GeoPoint mBaseLocation;
	private PoiManager mPoiManager;
	private TrackThread mThread;
	private boolean mThreadRunned = false;
	private OpenStreetMapView mOsmv;
	private Handler mMainMapActivityCallbackHandler;
	private boolean mStopDraw = false;

	protected ExecutorService mThreadExecutor = Executors.newSingleThreadExecutor();

	private class TrackThread extends Thread {

		@Override
		public void run() {
			Ut.d("run Default TrackThread");

			if(mPaths == null)
				mPaths = new ArrayList<Path>();
			else
				mPaths.clear();

			if(mTracks == null || mTracks.size() != MapConstants.TRACK_FILE_NUM){
				mTracks = mPoiManager.getDefaultTrackList();
				if(mTracks == null || mTracks.size() != MapConstants.TRACK_FILE_NUM){
					mThreadRunned = false;
					mStopDraw = true;
					return;
				}
			}
			Ut.d("Default track loaded");

			final OpenStreetMapViewProjection pj = mOsmv.getProjection();
			
			for(Track track: mTracks) {
				Path path = pj.toPixelsTrackPoints(track.getPoints(), mBaseCoords, mBaseLocation);
				mPaths.add(path);
			}

			Ut.d("Default track maped");

			Message.obtain(mMainMapActivityCallbackHandler, OpenStreetMapTileFilesystemProvider.MAPTILEFSLOADER_SUCCESS_ID).sendToTarget();

			mThreadRunned = false;
		}
	}

	public DefaultTrackOverlay(MainMapActivity mainMapActivity, PoiManager poiManager) {
		mPoiManager = poiManager;
		mBaseCoords = new Point();
		mBaseLocation = new GeoPoint(0, 0);
		mLastZoom = -1;
		mThread = new TrackThread();
		mThread.setName("Default track thread");


		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStrokeWidth(4);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setColor(mainMapActivity.getResources().getColor(R.color.track));
	}

	public void setStopDraw(boolean stopdraw){
		mStopDraw = stopdraw;
	}

	@Override
	protected void onDraw(Canvas c, OpenStreetMapView osmv) {
		if(mStopDraw) return;

		if (!mThreadRunned && (mTracks == null || mTracks.isEmpty() || mLastZoom != osmv.getZoomLevel())) {
			if(mPaths != null)
				mPaths.clear();
			mLastZoom = osmv.getZoomLevel();
			mMainMapActivityCallbackHandler = osmv.getHandler();
			mOsmv = osmv;
			//mThread.run();
			Ut.d("mThreadExecutor.execute "+mThread.isAlive());
			mThreadRunned = true;
			mThreadExecutor.execute(mThread);
			return;
		}

		if(mTracks == null || mTracks.size() != MapConstants.TRACK_FILE_NUM)
			return;

		if(mPaths == null || mPaths.isEmpty())
			return;

		Ut.d("Draw default track");
		final OpenStreetMapViewProjection pj = osmv.getProjection();
		final Point screenCoords = new Point();

		pj.toPixels(mBaseLocation, screenCoords);

		//final long startMs = System.currentTimeMillis();

		
		for (Path path: mPaths) {
			if(screenCoords.x != mBaseCoords.x && screenCoords.y != mBaseCoords.y){
				c.save();
				c.translate(screenCoords.x - mBaseCoords.x, screenCoords.y - mBaseCoords.y);
				c.drawPath(path, mPaint);
				c.restore();
			} else {
				c.drawPath(path, mPaint);
			}
		}
	}

	@Override
	protected void onDrawFinished(Canvas c, OpenStreetMapView osmv) {
	}

	public void clearTrack(){
		if(mTracks != null)
			mTracks.clear();
	}

}
