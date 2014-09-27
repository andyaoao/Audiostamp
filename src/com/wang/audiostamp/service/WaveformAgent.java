package com.wang.audiostamp.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import com.wang.audiostamp.application.RecordApplication;
import com.wang.audiostamp.object.TagObject;
import com.wang.audiostamp.object.WaveObject;
import com.wang.audiostamp.view.AudioDisplayView;
import com.wang.audiostamp.view.AudioTouchHandler;
import com.wang.audiostamp.view.AudioDisplayView.WaveformListener;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.ImageButton;

public class WaveformAgent extends Service {
	private Handler handler = new Handler();
	private AudioDisplayView mWaveformView;
	WaveObject mSoundFile;
	int mDensity = 1;
	int mMaxPos;
	public boolean needDraw = false;
	public String filePath;
	private List<TagObject> tagList;
	public class LocalBinder extends Binder {
		WaveformAgent getService() {
	        return  WaveformAgent.this;
	    }
	}
	private final IBinder mBinder = new LocalBinder();
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}
	
	@Override
	 public void onCreate(){
	   super.onCreate();
	 }
	 
	@Override
	public boolean onUnbind(Intent intent) {
		handler.removeCallbacks(drawThread);
		return super.onUnbind(intent);
	}
	
	 @Override
	 public void onDestroy() {	     
	     super.onDestroy();
	 }
	 
	 public void activeDrawThread(String filePath){
		 this.filePath = filePath;
		 needDraw = true;
		 WaveObject.getInstance().resetValue();
		 if(mWaveformView!=null){
			 mWaveformView.isViewing = false;
			 mWaveformView.resetValue(true);
		 }
		 handler.postDelayed(drawThread, 1000);
	 }
	 
	 public void activeBackDraw(String filePath){
		 this.filePath = filePath;
		 needDraw = false;
		 WaveObject.getInstance().resetValue();
		 if(mWaveformView!=null){
			 mWaveformView.isViewing = true;
			 mWaveformView.resetValue(false);
		 }
		 handler.post(drawThread);
	 }
	 
	 public List<TagObject> stopDrawThread(){
		 int maxOffset = mWaveformView.getMaxLevel();
		 needDraw = false;
		 if(mWaveformView!=null)
			 mWaveformView.isViewing = true;
		 List<TagObject> tagList = mWaveformView.tagList;
		 if(tagList != null){
			 for(int i=0; i<tagList.size(); i++){
				 TagObject tag = tagList.get(i);
				 tag.setTimeRatio(tag.getyOffset()/maxOffset);
			 }
		 }
		 return tagList;
	 }
	 
	 public void activeStampThread(){
		 if(needDraw)
			 handler.post(stampThread);
	 }
	 
	 private Runnable stampThread = new Runnable() {
		 public void run() {
			 //drawWaveform();
	         //Log.i("time:", new Date().toString());	         
	         RecordApplication app = ((RecordApplication)getApplicationContext());
	 		 mWaveformView = app.mDisplayView;
	 		 float x = mWaveformView.getMeasuredWidth()>>1;
	 		 float y = app.getLogoButton().getY()+ (app.getLogoButton().getHeight()/2);
	 		 mWaveformView.stampSound(x, y-25);
	     }
	};
	AudioDisplayView.WaveformListener audioListener;
	 private Runnable drawThread = new Runnable() {
		 public void run() {
			 RecordApplication app = ((RecordApplication)getApplicationContext());
	 		 mWaveformView = app.mDisplayView;
	 		 if(audioListener == null){
	 			audioListener = new AudioTouchHandler(mWaveformView);
	 		 }	 	
	 		 mWaveformView.setListener(audioListener);
	 		 if(needDraw){
	 			 waveformDraw(filePath, true);
	 			 handler.postDelayed(drawThread, 1000);
	 		 } else {
	 			waveformDraw(filePath, false);
	 		 }
	 		
	     }
	};
	
	public void setTagList(List<TagObject> tagList){
		this.tagList = tagList;
	}
	
	public void waveformDraw(String filePath, boolean isRawFile) {
		mSoundFile = WaveObject.getInstance();
		String sdPath = Environment.getExternalStorageDirectory().getPath();
		File soundFile = new File(filePath);
		if(soundFile!=null){
			try {
				if(!isRawFile)
					mSoundFile.readWaveFile(soundFile);
				else	
					mSoundFile.readRawFile(soundFile);
				//if (mSoundFile != null && !mWaveformView.hasSoundFile()) {
				RecordApplication myApp = ((RecordApplication)getApplicationContext());
				int cenHeight = (int) (myApp.getLogoButton().getY()+(myApp.getLogoButton().getHeight()>>1));
				mWaveformView.setWaveformHeight(cenHeight-20);
				mWaveformView.setSoundFile(mSoundFile);
				if(!isRawFile){
					mWaveformView.setTagList(tagList);
					mWaveformView.setNegativeTimeOffset(true, BackRecordService.backRecordTime);
				}
				mWaveformView.recomputeHeights(mDensity);
				((AudioTouchHandler)mWaveformView.getListener()).mMaxPos = mWaveformView.maxPos();
				//}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
}
