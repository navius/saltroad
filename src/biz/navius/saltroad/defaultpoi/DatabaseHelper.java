package biz.navius.saltroad.defaultpoi;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import biz.navius.saltroad.utils.RSQLiteOpenHelper;

public class DatabaseHelper extends RSQLiteOpenHelper {

	public DatabaseHelper(Context context, String name) {
		super(context, name, null, 1);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
