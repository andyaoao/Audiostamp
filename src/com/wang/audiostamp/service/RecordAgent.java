package com.wang.audiostamp.service;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wang.audiostamp.BaseActivity;
import com.wang.audiostamp.databases.SetDatabase;
import com.wang.audiostamp.fragment.PlayFragment;
import com.wang.audiostamp.fragment.RecordFragment;
import com.wang.audiostamp.object.AudioFiller;
import com.wang.audiostamp.object.AudioObject;
import com.wang.audiostamp.object.DateUtils;
import com.wang.audiostamp.object.SerializeUtils;
import com.wang.audiostamp.object.TagObject;
import com.wang.audiostamp.service.BackRecordService.MyIBinder;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import android.media.MediaPlayer;
import android.os.Binder;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

public class RecordAgent extends Service {
	private Handler handler = new Handler();
	WaveformAgent wavDisplayService;
	private final IBinder mBinder = new LocalBinder();
	BackRecordService backRecordService;
	private boolean isBackRecordBound = false;
	
	public class LocalBinder extends Binder {
		public RecordAgent getService() {
			return RecordAgent.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Intent intent = new Intent(getApplicationContext(), WaveformAgent.class);
		bindService(intent, connc, Context.BIND_AUTO_CREATE);
		bindBackRecordService();
		return mBinder;
	}

	public void recordStart() {
		handler.post(recordThread);
	}

	public void stampStart() {
		wavDisplayService.activeStampThread();
		//handler.post(stampThread);
	}

	public void recordStop() {
		handler.postDelayed(stopThread, 1000);
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		handler.removeCallbacks(recordThread);
		handler.removeCallbacks(stopThread);
		//handler.removeCallbacks(stampThread);
		unBindBackRecordService();
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private Runnable recordThread = new Runnable() {
		public void run() {
			final String outFile;
			if(isBackRecordBound)
				backRecordService.onStopRecord();
			AudioFiller audioFiller = AudioFiller.getInstance();
			outFile = audioFiller.startRecording();
			wavDisplayService.activeDrawThread(outFile);
		}
	};

	/*private Runnable stampThread = new Runnable() {
		public void run() {
			
		}
	};*/
	private void computeTagList(List<TagObject> tagList){
		AudioFiller audioFiller = AudioFiller.getInstance();
		File inFile = new File(audioFiller.getTempFilename());
		File backFile = new File(audioFiller.getBackRecordPath());
		long inSize = inFile.length();
		long backSize = backFile.length();
		long fileSize = inSize + backSize;
		if(tagList != null)
		for(int i=0; i<tagList.size(); i++){
			TagObject tag = tagList.get(i);
			tag.setTimeRatio(((tag.getTimeRatio()*inSize)+backSize)/fileSize);
		}
	}
	
	private Runnable stopThread = new Runnable() {
		public void run() {
			AudioFiller audioFiller = AudioFiller.getInstance();
			List<TagObject> tagList = wavDisplayService.stopDrawThread();
			
			computeTagList(tagList);
			
			String outFilePath = audioFiller.stopRecording();
			// StopRecord cause background record continuously
			backRecordService.onRecord();
			// TagList Restore
			/*if(tagList != null)
			for(int i=0; i<tagList.size(); i++){
				tagList.get(i).setTimeRatio(tagList.get(i).getTimeRatio()+ BackRecordService.backRecordRatio);
			}*/
			wavDisplayService.setTagList(tagList);
			wavDisplayService.activeBackDraw(outFilePath);
			saveToDB(outFilePath, tagList);
			Intent intent = new Intent();
			intent.setAction(BaseActivity.TAG);
			intent.putExtra(BaseActivity.PROGMSG, BaseActivity.WAIT_DONE);
			sendBroadcast(intent);
		}
	};

	private ServiceConnection connc = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			wavDisplayService = ((WaveformAgent.LocalBinder) service)
					.getService();
		}
	};

	// For Database storing
	@SuppressWarnings("unchecked")
	private void saveToDB(String fileName, List<TagObject> tagList) {
		File audioFile = new File(fileName);
		String name = audioFile.getName();

		AudioObject obj = new AudioObject();
		obj.setName(audioFile.getName());
		obj.setPath(audioFile.getAbsolutePath());
		obj.setDate(DateUtils.getDateString(Long.parseLong(name.substring(0,
				name.lastIndexOf(".")))));
		obj.setStamps("");

		MediaPlayer player = new MediaPlayer();
		try {
			player.setDataSource(obj.getPath());
			player.prepare();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		int milliseconds = player.getDuration();
		obj.setDuration(DateUtils.getDuratoin(milliseconds));
		player.release();
		if (tagList != null) {
			JSONArray jsonArray = new JSONArray();
			try {
				for (int i = 0; i < tagList.size(); i++) {
					//tagList.get(i).setTimeRatio(tagList.get(i).getTimeRatio() * (milliseconds/1000));
					JSONObject jsonObj;
					jsonObj = tagList.get(i).toJSONObject();
					jsonArray.put(jsonObj);
				}
				JSONObject json = new JSONObject();
				json.put("uniqueArrays", jsonArray);
				String arrayList = json.toString();
				obj.setStamps(arrayList);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		ArrayList<AudioObject> list = new ArrayList<AudioObject>();
		list.add(obj);
		Intent dbIntent = new Intent(PlayFragment.TAG);
		Bundle bundle = new Bundle();
		bundle.putParcelableArrayList(PlayFragment.AUDIOLIST_TEXT,
				(ArrayList<? extends Parcelable>) list);
		dbIntent.putExtras(bundle);
		sendBroadcast(dbIntent);
		// setDatabase.insertDataToFriend(list);
	}
	
	
	private ServiceConnection backRecordConnection = new ServiceConnection() {  
        public void onServiceConnected(android.content.ComponentName name,  
                android.os.IBinder service) {  
        	MyIBinder myIBinder = (MyIBinder) service;  
        	backRecordService = (BackRecordService) myIBinder.getService();  
            isBackRecordBound = true;  
            backRecordService.onRecord();
        };  
  
        public void onServiceDisconnected(android.content.ComponentName name) {  
        	isBackRecordBound = false;  
        };  
    };  
    
    private void unBindBackRecordService() {  
        if (isBackRecordBound) {  
            unbindService(backRecordConnection);  
            isBackRecordBound = false;  
        }  
    }  
  
    private void bindBackRecordService() {  
        Log.i("bind", "begin to bind backRecord");  
        Intent intent = new Intent(getApplicationContext(), BackRecordService.class);
        //intent.setAction(BackRecordService.TAG);
        bindService(intent, backRecordConnection, Context.BIND_AUTO_CREATE);  
    }  
}
