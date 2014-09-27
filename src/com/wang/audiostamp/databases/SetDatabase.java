package com.wang.audiostamp.databases;

import java.util.ArrayList;

import com.wang.audiostamp.object.AudioObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SetDatabase 
{
	private static final String TAG = SetDatabase.class.getSimpleName();
	
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	
	private Context mContext;
	
	public SetDatabase(Context context)
	{
		mContext = context;
	}
	
	public SetDatabase open() throws SQLException 
	{
		mDbHelper = new DatabaseHelper(mContext);
		mDb = mDbHelper.getWritableDatabase();
		
		return this;
	}
	
	public boolean insertDataToFriend(ArrayList<AudioObject> list)
	{
		if(isOpen() == false)
		{
			open();
		}
        
		long count = 0;
		
		try{
			mDb.beginTransaction();
			for(AudioObject fObject : list)
			{
				ContentValues values = new ContentValues();
				
				values.put(DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_NAME, fObject.getName());
				values.put(DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_DATE, fObject.getDate());
				values.put(DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_DURATION, fObject.getDuration());
				values.put(DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_STAMPS, fObject.getStamps());
				values.put(DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_PATH, fObject.getPath());
		
				count = mDb.insert(DatabaseConsts.DATABASE_TABLE_AS_RECORD,null,values);
		
				Log.i(TAG,"insert datas to record success ? "+ count );
			}
			mDb.setTransactionSuccessful();
		    
		}catch(Exception e){
			Log.i(TAG, e.toString());
		}finally{
			mDb.endTransaction();
		}
		
		close();
		
		return count > 0;
	}
	
	public boolean deleteDataFromFriend(ArrayList<AudioObject> list)
	{
		if(isOpen() == false)
		{
			open();
		}
		
		long count = 0;
		for(AudioObject fObject : list)
		{
			count = mDb.delete(DatabaseConsts.DATABASE_TABLE_AS_RECORD,DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_PATH +" = "+ fObject.getPath() , null);
		
			Log.i(TAG,"delete data from record success ? "+ count );
		}
		
		close();
		
		return count > 0;
	}
	
	public boolean updateDataToFriend(ArrayList<AudioObject> list)
	{
		if(isOpen() == false)
		{
			open();
		}
		
		long count = 0;
		for(AudioObject fObject : list)
		{
			ContentValues values = new ContentValues();
			values.put(DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_NAME, fObject.getName());
			values.put(DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_DATE, fObject.getDate());
			values.put(DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_DURATION, fObject.getDuration());
			values.put(DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_STAMPS, fObject.getStamps());
			values.put(DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_PATH, fObject.getPath());
		
			count = mDb.update(DatabaseConsts.DATABASE_TABLE_AS_RECORD, values, DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_PATH +" = "+ fObject.getPath() , null);
		
			Log.i(TAG,"update data to record success ? "+ count );
		}
		
		close();
		
		return count > 0;
	}
	
	public ArrayList<AudioObject> getAudioList()
	{
		if(isOpen() == false)
		{
			open();
		}
		
		ArrayList<AudioObject> mList = null;
		AudioObject fObject;
		
		//select * from friend
		Cursor cursor = mDb.query(DatabaseConsts.DATABASE_TABLE_AS_RECORD, 
				new String[]{},  
				null,
				null,
				null, 
				null, 
				DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_DATE + " ASC"); // TODO: Consider adding an order.
		   
		if(cursor == null)
		{
			Log.e(TAG, "Invalide query.");
			return mList;
		}
		else
		{
			mList = new ArrayList<AudioObject>();
		}
		   
		try{
			
			while (cursor.moveToNext())
			{
				fObject = new AudioObject();
				
				fObject.setSerial(cursor.getString(cursor.getColumnIndex(DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_SERIAL)));
				fObject.setName(cursor.getString(cursor.getColumnIndex(DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_NAME)));
				fObject.setDate(cursor.getString(cursor.getColumnIndex(DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_DATE)));
				fObject.setDuration(cursor.getString(cursor.getColumnIndex(DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_DURATION)));
				fObject.setStamps(cursor.getString(cursor.getColumnIndex(DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_STAMPS)));
				fObject.setPath(cursor.getString(cursor.getColumnIndex(DatabaseConsts.DATABASE_TABLE_AS_RECORD_COLUMN_PATH)));
				
				mList.add(fObject);
				
			}
			
		}catch(Exception e){
			Log.i(TAG, "Search record table error "+e.toString());
		}
		finally{
			cursor.close();
		}
		
		close();
		
		return mList;
	}
	
	public void close() 
	{
		mDbHelper.close();
	}

	public boolean isOpen()
	{
		return mDb.isOpen();
	}
	
/**
 * call this method when logout only
 * */	
	public void deleteTable()
	{
		mDbHelper.dropTable(mDb);
		
		mDbHelper.createTable(mDb);
	}
}
