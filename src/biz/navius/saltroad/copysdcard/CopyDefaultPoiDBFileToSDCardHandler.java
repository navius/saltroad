package biz.navius.saltroad.copysdcard;

import biz.navius.saltroad.MainMapActivity;
import biz.navius.saltroad.utils.Ut;
import android.os.Handler;
import android.os.Message;

public class CopyDefaultPoiDBFileToSDCardHandler extends Handler
{
	private MainMapActivity parentActivity = null; 
	
	public CopyDefaultPoiDBFileToSDCardHandler(MainMapActivity inParentActivity)
	{
		parentActivity = inParentActivity;
	}

	@Override
	public void handleMessage(Message msg) 
	{
		Boolean success = Ut.getBooleanFromABundle(msg.getData());
		this.setSuccessPreference(success);
	}

	private void setSuccessPreference(boolean success)
	{
		parentActivity.copyDefaultPoiDBFileToSDCardSuccessPreference(success);
	}
}
