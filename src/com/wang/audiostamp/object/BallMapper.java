package com.wang.audiostamp.object;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import android.widget.ImageButton;

import com.wang.audiostamp.R;
import com.wang.audiostamp.application.RecordApplication;
import com.wang.audiostamp.consts.StampColor;

public class BallMapper {
	//Map<Integer, Integer> ballMapper;
	List<Integer> resList;
	//List<ImageButton> btnList;
	static BallMapper thisPtr;
	RecordApplication shareView;
		
	public static BallMapper getInstance(){
		if(thisPtr == null)
			thisPtr = new BallMapper();
		return thisPtr;
	}
	
	BallMapper(){
		//ballMapper = new HashMap<Integer, Integer>();
		resList = new ArrayList<Integer>();
		//btnList = new ArrayList<ImageButton>();
	}
	
	
	
	public void buildColorMap(RecordApplication application){
		shareView = application;
		/*ballMapper.put(0, R.drawable.ss_blue);
		ballMapper.put(1, R.drawable.ss_green);
		ballMapper.put(2, R.drawable.ss_orange);
		ballMapper.put(3, R.drawable.ss_pink);
		ballMapper.put(4, R.drawable.ss_yellow);
		*/
		
//		resList.add(R.drawable.sss_blue);
//		resList.add(R.drawable.sss_green);
//		resList.add(R.drawable.sss_orange);
//		resList.add(R.drawable.sss_pink);
//		resList.add(R.drawable.sss_yellow);
		
		resList.add(StampColor.BLUE);
		resList.add(StampColor.GREEN);
		resList.add(StampColor.ORANGE);
		resList.add(StampColor.RED);
		resList.add(StampColor.YELLOW);
		
		/*for(int i=0; i<resList.size(); i++){
			ballMapper.put(resList.get(i), i);
		}*/
	}
	
	/*public int getColor(int resId){
		int color = 0;
		color = ballMapper.get(resId);
		return color;
	}*/
	
	public int rotateColor(){
		//int resId = resList.remove(0);
		int resId = resList.get(0);
		resList.remove(0);
		resList.add(resId);
		fillBtnList();
		return resId;
	}
	
	public int selectColor(int i){
		//int resId = resList.remove(i);
		//int firstId = resList.remove(0);
		int resId = resList.get(i);
		int firstId = resList.get(0);
		
		resList.remove(i);
		resList.remove(0);
		
		resList.add(0, resId);
		resList.add(i, firstId);
		fillBtnList();
		return resId;
	}
	
	public int getCurrentIndex(int resId){
		for(int i=0; i<resList.size(); i++){
			if(resList.get(i) == resId)
				return i;
		}
		return -1;
	}
	/*public int getCurrentColor(){
		return getColor(getCurrentRes());
	}*/
	
	public int getCurrentRes(){
		return resList.get(0);
	}
	
	private void fillBtnList(){
		int size = shareView.getColorBtnSize();
		for(int i=0; i<size;i++){
			ImageButton btn = shareView.getColorButton(i);
			int resId = getColorResId(resList.get(i));
			btn.setBackgroundResource(resId);
		}
	}
	
	public static int getColorResId(int colorIndex)
	{
		switch(colorIndex)
		{
			case StampColor.BLUE:
				return R.drawable.sss_blue;
			case StampColor.GREEN:
				return R.drawable.sss_green;
			case StampColor.ORANGE:
				return R.drawable.sss_orange;
			case StampColor.RED:
				return R.drawable.sss_pink;
			case StampColor.YELLOW:
				return R.drawable.sss_yellow;
			default:
				return R.drawable.sss_blue;					
		}
	}
}
