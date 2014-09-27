package com.wang.audiostamp.application;

import java.util.ArrayList;
import java.util.List;

import com.wang.audiostamp.view.AudioDisplayView;

import android.app.Application;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;

public class RecordApplication extends Application {
	private ImageButton mLogoButton;
	private ImageButton mStopButton;
	public AudioDisplayView mDisplayView;
	private EditText labelText;
	private List<ImageButton> colorBtn;
	private FrameLayout mRelLayout;
	
	
	public FrameLayout getRelLayout() {
		return mRelLayout;
	}
	public void setRelLayout(FrameLayout mRelLayout) {
		this.mRelLayout = mRelLayout;
	}
	public void setLogoButton(ImageButton imageButton){
		mLogoButton = imageButton;
	}
	public void setAudioDisplayView(AudioDisplayView displayView){
		mDisplayView = displayView;
	}
	public void setStopButton(ImageButton imageButton){
		mStopButton = imageButton;
	}
	public ImageButton getLogoButton(){
		return mLogoButton;
	}
	public ImageButton getStopButton(){
		return mStopButton;
	}
	public void setTextField(EditText editText){
		labelText = editText;
	}
	public EditText getTextField(){
		return labelText;
	}
	
	public void addColorButton(ImageButton btn){
		if(colorBtn == null)
			colorBtn = new ArrayList<ImageButton>();
		colorBtn.add(btn);
	}
	
	public ImageButton getColorButton(int index){
		return colorBtn.get(index);
	}
	
	public int getIndexFromBtnId(int resID){
		for(int i=0; i<colorBtn.size(); i++){
			if(colorBtn.get(i).getId() == resID)
				return i;
		}
		return -1;
	}
	
	public int getColorBtnSize(){
		return colorBtn.size();
	}
	
}
