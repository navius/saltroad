package biz.navius.saltroad.utils;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import biz.navius.saltroad.MainMapActivity;
import biz.navius.saltroad.R;

public class DownloadDefaultPoiImage extends AsyncTask<String, Integer, String> {
    private Context mCtx;
    private ProgressDialog mProgressDialog;
	private PowerManager.WakeLock myWakeLock = null;

    public DownloadDefaultPoiImage(Context context) {
		mCtx = context;
	}
    
	private void acquireWakeLock() {
		myWakeLock = ((PowerManager) mCtx.getSystemService(Context.POWER_SERVICE)).newWakeLock(
				PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "SaltRoad");
		myWakeLock.acquire();
	}
        
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
		mProgressDialog = new ProgressDialog(DownloadDefaultPoiImage.this.mCtx);
		mProgressDialog.setMessage(DownloadDefaultPoiImage.this.mCtx.getString(R.string.message_downloading_default_poi_image_file));
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();
    }

    @Override
    protected String doInBackground(String... urls) {
		acquireWakeLock();
        int count;
        final int SIZE = 1024*1024;

        try {
            URL url = new URL(urls[0]);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.connect();

            int lenghtOfFile = urlConnection.getContentLength();
            Ut.dd("Lenght of file: " + lenghtOfFile);

            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(urls[1]);

            byte data[] = new byte[SIZE];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                output.write(data, 0, count);
                publishProgress((int)((total*100)/lenghtOfFile));
            }

            output.flush();
            output.close();
            input.close();
            urlConnection.disconnect();
            return Integer.toString((int)total);
      	} catch (IOException e) {
  		// covers:
          //      ClientProtocolException
          //      ConnectTimeoutException
          //      ConnectionPoolTimeoutException
          //      SocketTimeoutException
          e.printStackTrace();
        }
        return null;
    }
    protected void onProgressUpdate(Integer... progress) {
    	mProgressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
		if (myWakeLock != null) {
			myWakeLock.release();
		}
    	mProgressDialog.dismiss();
    	
    	if (result == null) {
    		Dialog downloadFailedDlg = new
     	    AlertDialog.Builder(DownloadDefaultPoiImage.this.mCtx)
            .setIcon(0)
            .setTitle(DownloadDefaultPoiImage.this.mCtx.getString(R.string.error_failed_to_download_default_poi_image))
            .setPositiveButton("Ok", null)
            .create();
    		downloadFailedDlg.show();
    		return;
    	}
    	((MainMapActivity) mCtx).decompressDefaultPoiImageFile();
    }

}
