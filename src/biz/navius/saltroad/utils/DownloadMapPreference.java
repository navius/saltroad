package biz.navius.saltroad.utils;

import java.io.BufferedInputStream;
import java.io.File;
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
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import biz.navius.saltroad.R;
import biz.navius.saltroad.constants.MapConstants;

public class DownloadMapPreference extends Preference {
    private Button btnDownload;
    private Context mCtx;
    private String mMapFilePath = "";
    private String mMapName = "";
    private String mDownloadMapUrlString = "";
    private ProgressDialog mProgressDialog;

    public DownloadMapPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mCtx = context;
		setWidgetLayoutResource(R.layout.preference_widget_btn_download);
		
		final File folder = Ut.getRMapsMapsDir(mCtx);
	    String filename = "";
	    
	    Ut.dd(getKey());
	    if (getKey().equals("pref_download_medium_map")) {
		    filename = MapConstants.MEDIUM_MAP_FILE_NAME;
		    mMapName = MapConstants.MEDIUM_MAP_NAME;
	    }
	    else if (getKey().equals("pref_download_max_map")) {
		    filename = MapConstants.MAX_MAP_FILE_NAME;
		    mMapName = MapConstants.MAX_MAP_NAME;
	    }
	    
	    mMapFilePath = folder.getAbsolutePath() + File.separator + filename;
		mDownloadMapUrlString = MapConstants.MAP_URL_BASE + "/" + filename;
	}
    
    @Override
	protected void onBindView(View view) {
		super.onBindView(view);

		btnDownload = (Button) view.findViewById(R.id.btnDownload);
		btnDownload.setOnClickListener(new OnClickListener() {
			// @Override
			public void onClick(View v) {
				File mapFilePath = new File(mMapFilePath);
				if (mapFilePath.exists()) {
			    	AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
			    	builder.setTitle(mCtx.getString(R.string.warning_file_overwrite));
			    	WarningFileOverwriteAlertOnClickListener l = new WarningFileOverwriteAlertOnClickListener();
			    	builder.setPositiveButton("YES", l);
			    	builder.setNegativeButton("NO", l);
			    	
			    	AlertDialog ad = builder.create();
			    	ad.show();
			    	return;
				}
			    new DownloadFileAsync().execute(mDownloadMapUrlString, mMapFilePath);
			}
		});
	}

	private class WarningFileOverwriteAlertOnClickListener 
	implements android.content.DialogInterface.OnClickListener
	{
		public void onClick(DialogInterface v, int buttonId)
		{
			if (buttonId == DialogInterface.BUTTON_POSITIVE)
			{
			    new DownloadFileAsync().execute(mDownloadMapUrlString, mMapFilePath);
			}
		}
	}

    class DownloadFileAsync extends AsyncTask<String, Integer, String> {
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
			mProgressDialog = new ProgressDialog(DownloadMapPreference.this.mCtx);
			mProgressDialog.setMessage(DownloadMapPreference.this.mCtx.getString(R.string.message_downloading_map_file));
			mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mProgressDialog.setCancelable(false);
			mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {
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
        	mProgressDialog.dismiss();
        	
        	if (result == null) {
        		Dialog downloadFailedDlg = new
         	    AlertDialog.Builder(DownloadMapPreference.this.mCtx)
                .setIcon(0)
                .setTitle(DownloadMapPreference.this.mCtx.getString(R.string.error_failed_to_download_map))
                .setPositiveButton("Ok", null)
                .create();
        		downloadFailedDlg.show();
        		return;
        	}
        	
           	SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(DownloadMapPreference.this.mCtx);
    		SharedPreferences.Editor editor = pref.edit();
    		File mapFilePath = new File(mMapFilePath);
    		
    		String name = Ut.FileName2ID(mapFilePath.getName());
           	editor.putBoolean("pref_usermaps_" + name + "_enabled", true);
           	editor.putString("pref_usermaps_" + name + "_name", mMapName);
    		editor.commit();
        }
    }
}
