package com.wang.audiostamp.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.wang.audiostamp.object.AudioFiller;
import com.wang.audiostamp.object.WaveObject;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class BackRecordService extends Service {
	public static final String TAG = BackRecordService.class.getSimpleName();
	private Handler handler = new Handler();
	String outFile;
	private final IBinder mBinder = new MyIBinder();  
	public static final String RECORD_MSG = "RecordMsg";
	protected static final int NO_MSG = 0;
	public static final int START_RECORD = 1;
	protected static final int STOP_RECORD = 2;
	public static boolean isRecord = false;
	public static int backRecordLimit = 60; //300s means 5m
	public static int backRecordTime = 0;
	public static double backRecordRatio = 0;
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	@Override  
    public void onCreate() {  
        super.onCreate();
        File file = new File(AudioFiller.getInstance().getBackRecordPath());
        if(file.exists())
        	file.delete();
    }  
  
    @Override  
    public void onDestroy() {  
    	handler.removeCallbacks(recordThread);
        super.onDestroy();  
    }  
    
    @Override
	public boolean onUnbind(Intent intent) {
		
		return super.onUnbind(intent);
	}
    
    public void onRecord(){
    	if(!isRecord){
    		isRecord = true;
    		handler.post(recordThread);
    	}
    }
	/*@Override
	protected void onHandleIntent(Intent intent) {
		int msg = intent.getIntExtra(RECORD_MSG, NO_MSG);
		switch(msg){
		case START_RECORD:
			handler.post(recordThread);
			break;
		default:
			break;
		}
		
	}*/

    @Override
    public int onStartCommand(Intent intent, int flag, int startId) {
    	return super.onStartCommand(intent, flag, startId);
    }
    
	private Runnable recordThread = new Runnable() {
		public void run() {
			AudioFiller audioFiller = AudioFiller.getInstance();
			audioFiller.setIsBackRecord(true);
			audioFiller.setMemorySizeByTime(backRecordLimit);
			outFile = audioFiller.startRecording();
		}
	};
	
	public void onStopRecord() {
		if(isRecord){
			AudioFiller audioFiller = AudioFiller.getInstance();
			outFile = audioFiller.saveBackFile(audioFiller.getBackRecordPath());
			isRecord = false;
		}
	}
	
	public class MyIBinder extends Binder {  
        public BackRecordService getService() {  
            return BackRecordService.this;  
        }  
    }  
}
