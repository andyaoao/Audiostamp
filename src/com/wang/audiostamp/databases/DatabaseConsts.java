package com.wang.audiostamp.databases;

import com.wang.audiostamp.object.AudioObject;



public class DatabaseConsts 
{
	public final static String DATABASE_NAME = "audiostamp.db";
		
	public final static int DATABASE_VERSION = 1;
	
	//record table
	public final static String DATABASE_TABLE_AS_RECORD = "ASRecord";
	public final static String DATABASE_TABLE_AS_RECORD_COLUMN_SERIAL = AudioObject.TAG_SERIAL;
	public final static String DATABASE_TABLE_AS_RECORD_COLUMN_NAME = AudioObject.TAG_NAME;
	public final static String DATABASE_TABLE_AS_RECORD_COLUMN_DATE = AudioObject.TAG_DATE;
	public final static String DATABASE_TABLE_AS_RECORD_COLUMN_DURATION = AudioObject.TAG_DURATION;
	public final static String DATABASE_TABLE_AS_RECORD_COLUMN_STAMPS = AudioObject.TAG_STAMPS;
	public final static String DATABASE_TABLE_AS_RECORD_COLUMN_PATH = AudioObject.TAG_PATH;
}
