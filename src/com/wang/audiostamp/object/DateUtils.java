package com.wang.audiostamp.object;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;

public class DateUtils 
{
	private final static String TAG = DateUtils.class.getSimpleName();
	
	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "yyyy/MM/dd HH:mm" );	
	
	public DateUtils()
	{
		Log.i(TAG, "");
	}
	
	public static String getDateString(long time)
	{
		Date date = new Date(time);
		return DATE_FORMAT.format(date);
	}
	
	public static String getDuratoin(int milliseconds)
	{
		int seconds = (int) (milliseconds / 1000) % 60 ;
		String s = seconds > 10 ? String.valueOf(seconds) : "0"+seconds;
		
		int minutes = (int) ((milliseconds / (1000*60)) % 60);
		String m = minutes > 10 ? String.valueOf(minutes) : "0"+minutes;

		int hours   = (int) ((milliseconds / (1000*60*60)) % 24);

		if(hours > 0)
		{
			return hours+":"+m+":"+s;
		}
		else
		{
			return m+":"+s;
		}
		
	}
}
