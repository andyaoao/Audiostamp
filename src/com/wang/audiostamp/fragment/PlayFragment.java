package com.wang.audiostamp.fragment;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.wang.audiostamp.BaseActivity;
import com.wang.audiostamp.R;
import com.wang.audiostamp.adapter.AudioAdapter;
import com.wang.audiostamp.databases.SetDatabase;
import com.wang.audiostamp.object.AudioObject;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;


public class PlayFragment extends Fragment implements Runnable{
	private MediaPlayer mMediaPlayer;
	private ArrayList<AudioObject> mAudioFileList; 
	private ListView mPlaylistList;
	private AudioAdapter mAudioAdapter;
	private ImageButton mAudioPlay;
	private SetDatabase mSetDatabase;
	private LinearLayout mPlayMedia;
	private View baseView;
	private Thread mUpdateProgress;
	private ImageView mStampGreen;
	private boolean isStampGreen = false;
	private ImageView mStampBlue;
	private boolean isStampBlue = false;
	private ImageView mStampRed;
	private boolean isStampRed = false;
	private ImageView mStampOrange;
	private boolean isStampOrange = false;
	private ImageView mStampYellow;
	private boolean isStampYellow = false;
	private ImageView mStampAdd;
	private ImageView mMask;
	
	public final static String TAG = PlayFragment.class.getSimpleName();
	public static String AUDIOLIST_TEXT = "AudioList";
	public static PlayFragment getInstance() {
		PlayFragment thisPtr = new PlayFragment();
		return thisPtr;
    }
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
	
	@Override
	 public View onCreateView(LayoutInflater inflater, ViewGroup container,
	   Bundle savedInstanceState) {
		baseView = inflater.inflate(R.layout.play_layout, container, false);
		setPlaylistView(baseView);
		setStamplistView(baseView);
		baseView.getContext().registerReceiver(mBroadcast, new IntentFilter(TAG));
		
		IntentFilter updateMediaProgressFilter = new IntentFilter();
		updateMediaProgressFilter.addAction("update_media_progressbar");
		baseView.getContext().registerReceiver(UpdateMedidProgressBarReceiver, updateMediaProgressFilter);
		
		return baseView;
	 }
	
	private void setStamplistView(View view)
	{
		mMask = (ImageView) view.findViewById(R.id.tab_playlist_mask);
//		mMask.setOnTouchListener(new OnTouchListener()
//		{
//			@Override
//			public boolean onTouch(View arg0, MotionEvent arg1) 
//			{
//				if(mMask.getVisibility() == View.VISIBLE)
//				{
//					mMask.setVisibility(View.GONE);
//					return true;
//				}
//				else
//				{
//					return false;
//				}
//			}
//		});
		
		mStampGreen = (ImageView) view.findViewById(R.id.tab_playlist_stamp_green);
		mStampGreen.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0) 
			{
				if(isStampGreen)
				{
					isStampGreen = false;
					mStampGreen.setImageBitmap(null);
				}
				else
				{
					isStampGreen = true;
					mStampGreen.setImageResource(R.drawable.stamp_check);
				}
				
				showMask(mMask);
			}
		});
		
		mStampBlue = (ImageView) view.findViewById(R.id.tab_playlist_stamp_blue);
		mStampBlue.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0) 
			{
				if(isStampBlue)
				{
					isStampBlue = false;
					mStampBlue.setImageBitmap(null);
				}
				else
				{
					isStampBlue = true;
					mStampBlue.setImageResource(R.drawable.stamp_check);
				}
				
				showMask(mMask);
			}
		});
		
		mStampRed = (ImageView) view.findViewById(R.id.tab_playlist_stamp_pink);
		mStampRed.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0) 
			{
				if(isStampRed)
				{
					isStampRed = false;
					mStampRed.setImageBitmap(null);
				}
				else
				{
					isStampRed = true;
					mStampRed.setImageResource(R.drawable.stamp_check);
				}
				
				showMask(mMask);
			}
		});
		
		mStampOrange = (ImageView) view.findViewById(R.id.tab_playlist_stamp_orange);
		mStampOrange.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0) 
			{
				if(isStampOrange)
				{
					isStampOrange = false;
					mStampOrange.setImageBitmap(null);
				}
				else
				{
					isStampOrange = true;
					mStampOrange.setImageResource(R.drawable.stamp_check);
				}
				
				showMask(mMask);
			}
		});
		
		mStampYellow = (ImageView) view.findViewById(R.id.tab_playlist_stamp_yellow);
		mStampYellow.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View arg0) 
			{
				if(isStampYellow)
				{
					isStampYellow = false;
					mStampYellow.setImageBitmap(null);
				}
				else
				{
					isStampYellow = true;
					mStampYellow.setImageResource(R.drawable.stamp_check);
				}
				
				showMask(mMask);
			}
		});
		
		mStampAdd = (ImageView) view.findViewById(R.id.tab_playlist_stamp_add);
	}
	
	private void showMask(ImageView view)
	{
		if(isStampBlue || isStampGreen || isStampRed || isStampYellow || isStampOrange)
		{
			view.setVisibility(View.VISIBLE);
		}
		else
		{
			view.setVisibility(View.GONE);
		}
	}
	
	private void setPlaylistView(View view)
	{
		mMediaPlayer = new MediaPlayer();
		mUpdateProgress = new Thread(this);
		
		mSetDatabase = ((BaseActivity)getActivity()).getDataBase();
		mMediaPlayer.setOnCompletionListener(new OnCompletionListener()
		{
			@Override
			public void onCompletion(MediaPlayer mp)
			{
				//Toast.makeText(getActivity(), "OnCompletion", 10).show();
				AudioObject audio = mAudioFileList.get(mAudioAdapter.getExpendStampId());
				
				//mMediaPlayer.reset();
				mAudioAdapter.setMediaProgressValue(0,mMediaPlayer.getDuration());
				Log.i(TAG, "stamps:[ "+audio.getStamps()+" ]");
				try {
					mMediaPlayer.setDataSource(audio.getPath());
					mMediaPlayer.prepare();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				mAudioAdapter.setMediaProgressValue(0,mMediaPlayer.getDuration());
				mUpdateProgress = null;
			}
		});
		
		mAudioFileList = mSetDatabase.getAudioList();
		
		mPlaylistList = (ListView) view.findViewById(R.id.tab_playlist_list);
		mAudioAdapter = new AudioAdapter(view.getContext(), mAudioFileList);
		
		mPlaylistList.setAdapter(mAudioAdapter);
		mPlaylistList.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int pos, long id)
			{
				if(mAudioAdapter.getExpendStampId() == pos)
				{
					mAudioAdapter.setExpendStampId(-1);
					mAudioAdapter.setMediaProgressValue(0,mMediaPlayer.getDuration());
					mPlayMedia.setVisibility(View.GONE);
					if(mMediaPlayer!=null && mMediaPlayer.isPlaying())
					{
						mMediaPlayer.pause();
						mUpdateProgress = null;
					}
				}
				else
				{
					mAudioAdapter.setExpendStampId(pos);
					AudioObject audio = mAudioFileList.get(mAudioAdapter.getExpendStampId());
					
					mMediaPlayer.reset();
					mAudioAdapter.setMediaProgressValue(0,mMediaPlayer.getDuration());
					Log.i(TAG, "stamps:[ "+audio.getStamps()+" ]");
					try {
						mMediaPlayer.setDataSource(audio.getPath());
						mMediaPlayer.prepare();
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (SecurityException e) {
						e.printStackTrace();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					mPlayMedia.setVisibility(View.VISIBLE);
				}
				mAudioAdapter.notifyDataSetChanged();
			}
		});
		
		mAudioPlay = (ImageButton) view.findViewById(R.id.tab_playlist_play);
		mAudioPlay.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(mMediaPlayer.isPlaying())
				{
					mMediaPlayer.pause();
					mUpdateProgress = null;
					//Toast.makeText(getActivity(), "pause", 10).show();
					mAudioPlay.setImageResource(R.drawable.playback_play);
				}
				else
				{
					mMediaPlayer.start();
					if(mUpdateProgress == null)
					{
						mUpdateProgress = new Thread(PlayFragment.this);
					}
					mUpdateProgress.start();
					mAudioPlay.setImageResource(R.drawable.playback_stop);
					//Toast.makeText(getActivity(), "play", 10).show();
				}
			}
		});
		
		mPlayMedia = (LinearLayout) view.findViewById(R.id.tab_playlist_media);
	}
	
	private BroadcastReceiver mBroadcast =  new BroadcastReceiver() {       
        @Override
        public void onReceive(Context mContext, Intent mIntent) {
        	Bundle bundle = mIntent.getExtras();
        	ArrayList farList = bundle.getParcelableArrayList(AUDIOLIST_TEXT);
        	ArrayList<AudioObject> list = (ArrayList<AudioObject>) farList;
        	mSetDatabase.insertDataToFriend(list);
        	setPlaylistView(baseView);
        }
    };
	
    
    
    @Override
	public void run() 
	{
    	int currentPosition= 0;
        int total = mMediaPlayer.getDuration();
        while (mMediaPlayer.isPlaying() && currentPosition<total) {
            try {
                Thread.sleep(1000);
                currentPosition= mMediaPlayer.getCurrentPosition();
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                return;
            }
            
            if(mAudioAdapter!=null)
            {
            	mAudioAdapter.updateProgressBar(mAudioAdapter.getExpendStampId(), currentPosition,total);
            	Intent i = new Intent("update_media_progressbar");
            	i.setAction("update_media_progressbar");
            	i.putExtra("value1", currentPosition);
            	i.putExtra("value2", total);
            	getActivity().sendBroadcast(i);
            }
        }
        
	}
    
	private BroadcastReceiver UpdateMedidProgressBarReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			String action = intent.getAction();
			if(action.equals("update_media_progressbar"))  
			{	
				if(mAudioAdapter!=null)
				{
//					int pos = intent.getIntExtra("value1", -1);
//					int total = intent.getIntExtra("value2", -1);
//					Toast.makeText(getActivity(), "update:"+pos+"/"+total, 1).show();
					
					mAudioAdapter.notifyDataSetChanged();
				}
			}
		}
		
	};
}
