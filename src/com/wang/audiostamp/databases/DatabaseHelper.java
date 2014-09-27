package com.wang.audiostamp.databases;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper
{
	private final static String TAG = DatabaseHelper.class.getSimpleName();
	
	public DatabaseHelper(Context context) 
	{
		super(context, DatabaseConsts.DATABASE_NAME, null, DatabaseConsts.DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{    		   		
		createTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
	}
	
	public void createTable(SQLiteDatabase db)
	{
		createRecordTable(db);
	}
	
	private void createRecordTable(SQLiteDatabase db)
	{
//create record table
		db.execSQL("CREATE TABLE " + DatabaseConsts.DATABASE_TABLE_AS_RECORD + " ("
                   + DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_SERIAL  + " INTEGER PRIMARY KEY,"
                   + DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_NAME  + " TEXT,"
                   + DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_DATE  + " TEXT,"
                   + DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_DURATION  + " TEXT,"
                   + DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_STAMPS  + " TEXT,"
                   + DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_PATH  + " TEXT"
                   + ");");
		
	}
	
	public void dropTable(SQLiteDatabase db)
	{
		db.execSQL("DROP TABLE IF EXISTS " + DatabaseConsts.DATABASE_TABLE_AS_RECORD);
	}
	
}


