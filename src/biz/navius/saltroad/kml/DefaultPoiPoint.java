package biz.navius.saltroad.kml;

import org.andnav.osm.util.GeoPoint;
import biz.navius.saltroad.kml.constants.PoiConstants;

public class DefaultPoiPoint implements PoiConstants {

	private final int Id;
	public String Title;
	public String Descr;
	public GeoPoint GeoPoint;
	public String IconName;
	public double Alt;
	public String CategoryCode;
	public int PointSourceId;
	public boolean Hidden;
	public String ImageName;

	public DefaultPoiPoint(int id, String mTitle, String mDescr, GeoPoint mGeoPoint,
			String iconname, String categorycode, double alt, int sourseid, int hidden, String mImageName) {
		this.Id = id;
		this.Title = mTitle;
		this.Descr = mDescr;
		this.GeoPoint = mGeoPoint;
		this.IconName = iconname;
		this.Alt = alt;
		this.CategoryCode = categorycode;
		this.PointSourceId = sourseid;
		this.Hidden = hidden == 1 ? true : false;
		this.ImageName = mImageName;
	}

	public DefaultPoiPoint(){
		this(EMPTY_ID, "", "", null, "default_poi_icon", "800", 0, 0, 0, "");
	}

	public DefaultPoiPoint(int id, String mTitle, String mDescr, GeoPoint mGeoPoint, String categorycode, String iconname, String mImageName) {
		this(id, mTitle, mDescr, mGeoPoint, iconname, categorycode, 0, 0, 0, mImageName);
	}

	public DefaultPoiPoint(String mTitle, String mDescr, GeoPoint mGeoPoint, String iconname, String mImageName) {
		this(EMPTY_ID, mTitle, mDescr, mGeoPoint, iconname, "800", 0, 0, 0, mImageName);
	}

	public int getId() {
		return Id;
	}

	public static int EMPTY_ID(){
		return EMPTY_ID;
	}

}
