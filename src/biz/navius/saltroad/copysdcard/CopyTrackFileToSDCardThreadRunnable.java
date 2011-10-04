package biz.navius.saltroad.copysdcard;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.openintents.filemanager.util.FileUtils;
import org.xml.sax.SAXException;

import biz.navius.saltroad.MainMapActivity;
import biz.navius.saltroad.constants.MapConstants;
import biz.navius.saltroad.kml.PoiManager;
import biz.navius.saltroad.kml.XMLparser.GpxTrackParser;
import biz.navius.saltroad.kml.XMLparser.KmlTrackParser;
import biz.navius.saltroad.utils.Ut;
import android.os.Handler;
import android.os.Message;

public class CopyTrackFileToSDCardThreadRunnable implements Runnable
{
	Handler mainThreadHandler = null;
	private MainMapActivity parentActivity = null;
	PoiManager mPoiManager = null;
	
	public CopyTrackFileToSDCardThreadRunnable(Handler h, MainMapActivity inParentActivity, PoiManager p)
	{
		mainThreadHandler = h;
		parentActivity = inParentActivity;
		mPoiManager = p;
	}

	public void run()
	{
    	copyTrackFileToSDCard();
	}
	
	private void copyTrackFileToSDCard() {
		Message m = this.mainThreadHandler.obtainMessage();
	    File folder = Ut.getRMapsImportDir(parentActivity);

		for(Integer i = 0 ; i < MapConstants.TRACK_FILE_NUM ; i++) {
	        String basename = String.format("%s%02d", MapConstants.DEFAULT_TRACK_FILE_BASE_NAME, i + 1);
	        String filename = basename + ".kml";
			String filePath = folder.getAbsolutePath() + File.separator + filename;
			int trackKMLID = parentActivity.getResources().getIdentifier(basename, "raw", parentActivity.getPackageName());
			
			if (!Ut.cpSDCard(parentActivity, trackKMLID, filePath)) {
				m.setData(Ut.getBooleanAsABundle(false));
				this.mainThreadHandler.sendMessage(m);
				return;
			}
	    }

	    for(Integer i = 0 ; i < MapConstants.TRACK_FILE_NUM ; i++) {
	        String basename = String.format("%s%02d", MapConstants.DEFAULT_TRACK_FILE_BASE_NAME, i + 1);
	        String filename = basename + ".kml";
			String filePath = folder.getAbsolutePath() + File.separator + filename;
			if (!importTrack(filePath)) {
				m.setData(Ut.getBooleanAsABundle(false));
				this.mainThreadHandler.sendMessage(m);
				return;
			}
		}
	    
		m.setData(Ut.getBooleanAsABundle(true));
		this.mainThreadHandler.sendMessage(m);
		return;
	}
	
	private boolean importTrack(final String fileName) {
		File file = new File(fileName);

		if(!file.exists()){
			return false;
		}
		
		SAXParserFactory fac = SAXParserFactory.newInstance();
		SAXParser parser = null;
		try {
			parser = fac.newSAXParser();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		if(parser != null){
			mPoiManager.beginTransaction();
			Ut.dd("Start parsing file " + file.getName());
			try {
				if(FileUtils.getExtension(file.getName()).equalsIgnoreCase(".kml"))
					parser.parse(file, new KmlTrackParser(mPoiManager));
				else if(FileUtils.getExtension(file.getName()).equalsIgnoreCase(".gpx"))
					parser.parse(file, new GpxTrackParser(mPoiManager));

				mPoiManager.commitTransaction();
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mPoiManager.rollbackTransaction();
				return false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				mPoiManager.rollbackTransaction();
				return false;
			} catch (IllegalStateException e) {
				return false;
			} catch (OutOfMemoryError e) {
				Ut.w("OutOfMemoryError");
				mPoiManager.rollbackTransaction();
				return false;
			}
			Ut.dd("Pois commited");
		}
		return true;
	}
}
