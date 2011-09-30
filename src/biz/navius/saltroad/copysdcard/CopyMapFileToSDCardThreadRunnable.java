package biz.navius.saltroad.copysdcard;

import java.io.File;

import biz.navius.saltroad.MainMapActivity;
import biz.navius.saltroad.R;
import biz.navius.saltroad.constants.MapConstants;
import biz.navius.saltroad.utils.Ut;
import android.os.Handler;
import android.os.Message;

public class CopyMapFileToSDCardThreadRunnable implements Runnable
{
	Handler mainThreadHandler = null;
	private MainMapActivity parentActivity = null;
	
	public CopyMapFileToSDCardThreadRunnable(Handler h, MainMapActivity inParentActivity)
	{
		mainThreadHandler = h;
		parentActivity = inParentActivity;
	}

	public void run()
	{
    	copyMapFileToSDCard();
	}
	
	private void copyMapFileToSDCard() {
		Message m = this.mainThreadHandler.obtainMessage();
		
	    File folder = Ut.getRMapsMapsDir(parentActivity);
	      
	    String basename = MapConstants.MAP_FILE_NAME;
		String filePath = folder.getAbsolutePath() + File.separator + basename;
		
		if (!Ut.cpSDCard(parentActivity, R.raw.offline_map_min_11_14, filePath)) {
			m.setData(Ut.getBooleanAsABundle(false));
			this.mainThreadHandler.sendMessage(m);
			return;
		}
		m.setData(Ut.getBooleanAsABundle(true));
		this.mainThreadHandler.sendMessage(m);
		return;
	}
}
