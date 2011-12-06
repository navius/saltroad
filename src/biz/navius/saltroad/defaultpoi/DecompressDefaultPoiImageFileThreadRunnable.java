package biz.navius.saltroad.defaultpoi;

import java.io.File;

import biz.navius.saltroad.MainMapActivity;
import biz.navius.saltroad.constants.MapConstants;
import biz.navius.saltroad.utils.Decompress;
import biz.navius.saltroad.utils.Ut;
import android.os.Handler;
import android.os.Message;

public class DecompressDefaultPoiImageFileThreadRunnable implements Runnable
{
	Handler mainThreadHandler = null;
	private MainMapActivity parentActivity = null;
	
	public DecompressDefaultPoiImageFileThreadRunnable(Handler h, MainMapActivity inParentActivity)
	{
		mainThreadHandler = h;
		parentActivity = inParentActivity;
	}

	public void run()
	{
    	copyDefaultPoiDBFileToSDCard();
	}
	
	private void copyDefaultPoiDBFileToSDCard() {
		Message m = this.mainThreadHandler.obtainMessage();

		File zipFileFolder = Ut.getRMapsTmpDir(parentActivity);
		String zipFilename = zipFileFolder.getAbsolutePath() + File.separator + MapConstants.DEFAULT_POI_IMAGE_FILES_ZIP_FILE_NAME;
		
		File unzipFolder = Ut.getRMapsDefaultPoiImageDir(parentActivity);
		String unzipLocation = unzipFolder.getAbsolutePath() + File.separator;

		Decompress d = new Decompress(zipFilename, unzipLocation); 
		
		if (!d.unzip()) {
			m.setData(Ut.getBooleanAsABundle(false));
			this.mainThreadHandler.sendMessage(m);
			return;
		}
		m.setData(Ut.getBooleanAsABundle(true));
		this.mainThreadHandler.sendMessage(m);
		return;
	}
}
