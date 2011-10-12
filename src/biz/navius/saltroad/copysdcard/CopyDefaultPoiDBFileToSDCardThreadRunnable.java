package biz.navius.saltroad.copysdcard;

import java.io.File;

import biz.navius.saltroad.MainMapActivity;
import biz.navius.saltroad.R;
import biz.navius.saltroad.constants.MapConstants;
import biz.navius.saltroad.utils.Ut;
import android.os.Handler;
import android.os.Message;

public class CopyDefaultPoiDBFileToSDCardThreadRunnable implements Runnable
{
	Handler mainThreadHandler = null;
	private MainMapActivity parentActivity = null;
	
	public CopyDefaultPoiDBFileToSDCardThreadRunnable(Handler h, MainMapActivity inParentActivity)
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
		
		File folder = Ut.getRMapsMainDir(parentActivity, "data");
	    String basename = MapConstants.DEFAULTPOI_DB;
		String filePath = folder.getAbsolutePath() + File.separator + basename;
		
		if (!Ut.cpSDCard(parentActivity, R.raw.defaultpoi, filePath)) {
			m.setData(Ut.getBooleanAsABundle(false));
			this.mainThreadHandler.sendMessage(m);
			return;
		}
		m.setData(Ut.getBooleanAsABundle(true));
		this.mainThreadHandler.sendMessage(m);
		return;
	}
}
