package com.wang.audiostamp;

import com.viewpagerindicator.IconPageIndicator;
import com.viewpagerindicator.PageIndicator;
import com.wang.audiostamp.R;
import com.wang.audiostamp.R.id;
import com.wang.audiostamp.R.layout;
import com.wang.audiostamp.R.menu;
import com.wang.audiostamp.databases.SetDatabase;
import com.wang.audiostamp.fragment.RecordFragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class BaseActivity extends FragmentActivity {
	ASPageAdapter mAdapter;
	ViewPager mPager;
	PageIndicator mIndicator;
	private static ProgressDialog progressDialog;
	private boolean isTransDone;
	public static String PROGMSG = "PROGRESSING";
	public static final int WAIT_NONE = 0;
	public static final int WAIT_PROGRESS = 1;
	public static final int WAIT_DONE = 2;
	public final static String TAG = BaseActivity.class.getSimpleName();
	//Preferences	
	protected SharedPreferences sharedPref;
	protected SharedPreferences.Editor sharedEditor;

	//Database
	protected SetDatabase mSetDatabase;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mAdapter = new ASPageAdapter(this.getSupportFragmentManager());

		mPager = (ViewPager) findViewById(R.id.pager);
		mPager.setAdapter(mAdapter);
		
		mIndicator = (IconPageIndicator) findViewById(R.id.indicator);
		mIndicator.setViewPager(mPager);
		//mIndicator.setOnPageChangeListener(mPageChangeListener);
		initSharedPreferences();
		initDatabase();
		registerReceiver(mBroadcast, new IntentFilter(TAG));
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
	
	private void initSharedPreferences() 
	{
		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		sharedEditor = sharedPref.edit();
		
		sharedEditor.commit();
	}
	
	private void initDatabase()
	{
		if(mSetDatabase == null)
		{
			mSetDatabase = new SetDatabase(this).open();
		}
	}
	public SetDatabase getDataBase(){
		return mSetDatabase;
	}
	
	
	private BroadcastReceiver mBroadcast =  new BroadcastReceiver() {       
        @Override
        public void onReceive(Context mContext, Intent mIntent) {
        	int msg = mIntent.getIntExtra(PROGMSG, WAIT_NONE);
        	
        	switch(msg){
        	case WAIT_DONE:
        	if(progressDialog != null){
        		progressDialog.dismiss();
        	}
        	break;
        	case WAIT_PROGRESS:
				if(progressDialog == null)
					progressDialog = new ProgressDialog(BaseActivity.this);
				isTransDone = false;
        		progressDialog.setTitle("Progressing");
        		progressDialog.setMessage("Wait for restore recording data...");
        		progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        		progressDialog.show();
        		break;
        	default:
        		break;
        	}
        }
    };

}
