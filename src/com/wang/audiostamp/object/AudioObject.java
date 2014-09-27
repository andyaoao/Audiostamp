package com.wang.audiostamp.object;

import java.util.List;

public class AudioObject extends BaseObject
{
	@SuppressWarnings("unused")
	private final static String TAG = AudioObject.class.getSimpleName();

	private final static long serialVersionUID = 1L;
	
	public final static String TAG_SERIAL = "serial";
	public final static String TAG_NAME = "name";
	public final static String TAG_DATE = "date";
	public final static String TAG_DURATION = "duration";
	public final static String TAG_STAMPS = "stamp";
	public final static String TAG_PATH = "path";
	
	private String serial;
	private String name;
	private String date;
	private String duration;
	private String stamps;
	private String path;
	
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getStamps() {
		return stamps;
	}

	public void setStamps(String stamps) {
		this.stamps = stamps;
	}

	public String getSerial() {
		return serial;
	}
	
	public void setSerial(String id) {
		this.serial = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	
	
}
