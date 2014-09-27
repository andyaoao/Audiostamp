package com.wang.audiostamp.fragment;


import com.wang.audiostamp.BaseActivity;
import com.wang.audiostamp.R;
import com.wang.audiostamp.application.RecordApplication;
import com.wang.audiostamp.object.AudioFiller;
import com.wang.audiostamp.object.BallMapper;
import com.wang.audiostamp.service.RecordAgent;
import com.wang.audiostamp.view.AudioDisplayView;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.ColorMatrixColorFilter;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;


public class RecordFragment extends Fragment {
	
	RecordAgent audioMgrService;
	boolean goRecord = true;
	private View myFragmentView;
	static RecordFragment thisPtr;
	private Handler handler = new Handler();
	private boolean isExtend;
	

	private final static float[] BUTTON_PRESSED = new float[] {      
		  1.1f, 0, 0, 0, -20,      
	      0, 1.1f, 0, 0, -20,      
	      0, 0, 1.1f, 0, -20,      
	      0, 0, 0, 1.2f, 0 };
	
	public static RecordFragment getInstance() {
		if(thisPtr == null)
			thisPtr = new RecordFragment();
		return thisPtr;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	private ServiceConnection connc = new ServiceConnection() {
		@Override
		public void onServiceDisconnected(ComponentName name) {
		}
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			audioMgrService = ((RecordAgent.LocalBinder) service).getService();
		}
	};

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		myFragmentView = inflater.inflate(R.layout.record_layout, container,
				false);
		final Context context = myFragmentView.getContext();
		final RecordApplication myApp = ((RecordApplication) context
				.getApplicationContext());
		myApp.setLogoButton((ImageButton) myFragmentView
				.findViewById(R.id.logoBtn));
		myApp.setStopButton((ImageButton) myFragmentView
				.findViewById(R.id.stopBtn));
		myApp.setTextField((EditText) myFragmentView
				.findViewById(R.id.labelText));
		myApp.setAudioDisplayView((AudioDisplayView) myFragmentView
				.findViewById(R.id.waveform));
		myApp.getTextField().setEnabled(false);
		
		myApp.setRelLayout((FrameLayout) myFragmentView.findViewById(R.id.relLayout));
		
		myApp.addColorButton((ImageButton) myFragmentView.findViewById(R.id.btnL1));
		myApp.addColorButton((ImageButton) myFragmentView.findViewById(R.id.btnL2));
		myApp.addColorButton((ImageButton) myFragmentView.findViewById(R.id.btnL3));
		myApp.addColorButton((ImageButton) myFragmentView.findViewById(R.id.btnL4));
		myApp.addColorButton((ImageButton) myFragmentView.findViewById(R.id.btnL5));
		
		final Intent intent = new Intent(context, RecordAgent.class);
		intent.putExtra("RECORD", goRecord);
		context.bindService(intent, connc, Context.BIND_AUTO_CREATE);
		
		// Logo Button Click Event
		myApp.getLogoButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Toast.makeText(context.getApplicationContext(), "Recording..",
				//		Toast.LENGTH_LONG).show();
				if (goRecord) {					
					v.setBackgroundResource(R.drawable.bt_center02);
					audioMgrService.recordStart();
					goRecord = !goRecord;
					myApp.getStopButton().setVisibility(View.VISIBLE);
					myApp.getRelLayout().setVisibility(View.VISIBLE);
				} else {
					//Toast.makeText(context.getApplicationContext(), "Stamping", 100).show();
					audioMgrService.stampStart();
				}
			}
		});
		
		// Stop Button Click Event
		myApp.getStopButton().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				//Toast.makeText(context.getApplicationContext(), "StopRecord..",
				//		Toast.LENGTH_LONG).show();
				if (!goRecord) {	
					Intent intent = new Intent();
					intent.setAction(BaseActivity.TAG);
					intent.putExtra(BaseActivity.PROGMSG, BaseActivity.WAIT_PROGRESS);
					v.getContext().sendBroadcast(intent);					
					audioMgrService.recordStop();
					myApp.getLogoButton().setBackgroundResource(R.drawable.bt_center01);
					goRecord = !goRecord;
					myApp.getStopButton().setVisibility(View.INVISIBLE);
					myApp.getRelLayout().setVisibility(View.INVISIBLE);
				}
			}
		});
		
		
		
		final OnTouchListener touchListener = new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if(arg1.getAction() == MotionEvent.ACTION_DOWN) {
					arg0.getBackground().setColorFilter(new ColorMatrixColorFilter(BUTTON_PRESSED));
					arg0.setBackgroundDrawable(arg0.getBackground());
				}else if(arg1.getAction() == MotionEvent.ACTION_UP) {
					arg0.getBackground().clearColorFilter();
				}
				return false;
			}
		};
		
		
		final OnClickListener ballClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				int curId = v.getId();
				if(curId == R.id.btnL1){
					int i;
					if(!isExtend){
						for(i=1; i<myApp.getColorBtnSize(); i++){
							myApp.getColorButton(i).setVisibility(View.VISIBLE);
						}
						isExtend = !isExtend;
					} else {
						for(i=1; i<myApp.getColorBtnSize(); i++){
							myApp.getColorButton(i).setVisibility(View.INVISIBLE);
						}
						isExtend = !isExtend;
					}
				} else {
					BallMapper ballMapper = BallMapper.getInstance();
					int oldIndex = myApp.getIndexFromBtnId(curId);
					ballMapper.selectColor(BallMapper.getColorResId(oldIndex));
					
				}
			}
		};
		for(int i=0; i<myApp.getColorBtnSize(); i++){
			myApp.getColorButton(i).setOnClickListener(ballClickListener);
			if(i>0)
				myApp.getColorButton(i).setVisibility(View.INVISIBLE);
		}
		isExtend = false;
		myApp.getStopButton().setOnTouchListener(touchListener);
		myApp.getLogoButton().setOnTouchListener(touchListener);
		
		return myFragmentView;
	}
}
