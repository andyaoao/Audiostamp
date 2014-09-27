package com.wang.audiostamp.adapter;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.wang.audiostamp.R;
import com.wang.audiostamp.object.AudioObject;
import com.wang.audiostamp.object.BallMapper;
import com.wang.audiostamp.object.TagObject;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AudioAdapter extends BaseListAdapter<AudioObject>
{
	private final static String	TAG = AudioAdapter.class.getSimpleName();
	
	private int mExpendStampId = -1;
	private int mMediaProgressVaule = -1;
	private int mMediaDuration = -1;
	
	public AudioAdapter(Context context, ArrayList<AudioObject> list) 
	{
		super(context, list);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		final AudioItemView commentView;	
		if(convertView == null)
		{
			convertView = LayoutInflater.from(mContext).inflate(R.layout.audio_item , parent , false);

			commentView = new AudioItemView();
			
			commentView.mName = (TextView) convertView.findViewById(R.id.audio_item_title);
			commentView.mDuration = (TextView) convertView.findViewById(R.id.audio_item_duration);

			commentView.mProgressbar = (ProgressBar) convertView.findViewById(R.id.audio_item_progressbar);
			commentView.mPlayback = (LinearLayout) convertView.findViewById(R.id.audio_item_progress_stamps_layout);
			
			commentView.mDate = (TextView) convertView.findViewById(R.id.audio_item_date);
			commentView.mStampsLayout = (LinearLayout) convertView.findViewById(R.id.audio_item_stamps_layout);
			
			
			convertView.setTag(commentView);
		}
		else
		{
			commentView = (AudioItemView)convertView.getTag();
		}
		
		commentView.mName.setText(list.get(position).getName());
		commentView.mDate.setText(list.get(position).getDate());
		commentView.mDuration.setText(list.get(position).getDuration());
		
		commentView.mPlayback.removeAllViews();
		
		commentView.mStampsLayout.removeAllViews();
		String stampStr = list.get(position).getStamps();
		  
		if(stampStr != null && stampStr.length()>0){
			JSONObject json;
			try {
				json = new JSONObject(stampStr);
				JSONArray jsonArray = json.optJSONArray("uniqueArrays");
				int jsonCount = jsonArray.length();
				for(int i = 0; i< jsonCount; i++){
					JSONObject obj = (JSONObject) jsonArray.get(i);
					TagObject tag = new TagObject((double) 0, "", 0);
					tag = tag.toTagObject(obj);
					
					TextView test = new TextView(mContext);
					test.setTextColor(Color.BLACK);
					test.setGravity(Gravity.CENTER_VERTICAL);
					test.setText(tag.getText()+":"+tag.getTimeRatio());
					test.setCompoundDrawablesWithIntrinsicBounds(BallMapper.getColorResId(tag.getColorRes()), 0, 0, 0);
					
					commentView.mStampsLayout.addView(test);
					
					//LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
					LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT);
					lp.weight = (int)(tag.getTimeRatio()*100);
					ImageView stamp = new ImageView(mContext);
					stamp.setImageResource(BallMapper.getColorResId(tag.getColorRes()));
					stamp.setLayoutParams(lp);
					commentView.mPlayback.addView(stamp);
					
				}
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		
		if(mExpendStampId == position)
		{
			commentView.mStampsLayout.setVisibility(View.VISIBLE);
			commentView.mProgressbar.setPressed(true);
			if(mMediaProgressVaule == 0)
			{
				commentView.mProgressbar.setProgress(0);
			}
			else
			{
				int p = (int)((mMediaProgressVaule*100) / mMediaDuration);
				commentView.mProgressbar.setProgress(p);
			}
			
			convertView.setBackgroundColor(0xAA00FFFF);
		}
		else
		{
			commentView.mStampsLayout.setVisibility(View.GONE);
			commentView.mProgressbar.setProgress(0);
			commentView.mProgressbar.setPressed(false);
			
			convertView.setBackgroundColor(Color.TRANSPARENT);
		}
		
		return convertView;
	}

	public int getExpendStampId()
	{
		return mExpendStampId;
	}
	
	public void setExpendStampId(int id)
	{
		mExpendStampId = id;
	}
	
	public void updateProgressBar(int id, int progress, int duration)
	{
		if(id == mExpendStampId)
		{
			setMediaProgressValue(progress,duration);
		}
		else
		{
			mMediaProgressVaule = 0;
			mMediaDuration = 0;
		}
	}
	
	public void setMediaProgressValue(int progress, int duration)
	{
		mMediaProgressVaule = progress;
		mMediaDuration = duration;
	}
	
	public class AudioItemView
	{
		public TextView mName;
		public TextView mDuration;
		public LinearLayout mPlayback;
		public TextView mDate;
		public LinearLayout mStampsLayout;
		public ProgressBar mProgressbar;
	}

}
