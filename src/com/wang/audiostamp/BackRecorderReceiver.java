package com.wang.audiostamp;

import com.wang.audiostamp.service.BackRecordService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class BackRecorderReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		 if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
			 Log.i("BackRecord", "START");
			 Intent backIntent = new Intent(context, BackRecordService.class);
			 backIntent.putExtra(BackRecordService.RECORD_MSG, BackRecordService.START_RECORD);
			 backIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	         context.startService(backIntent);
			 Toast.makeText(context, "OlympicsReminder service has started!", Toast.LENGTH_LONG).show();
	     } else if(intent.getAction().equals(BackRecordService.TAG)){
	    	 Log.i("BackRecord", "STOP");
	    	 
	     }
	}

}
