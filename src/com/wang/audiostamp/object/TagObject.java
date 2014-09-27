package com.wang.audiostamp.object;

import org.json.JSONException;
import org.json.JSONObject;

public class TagObject{
	private Double yOffset;
	private String text;
	private int colorRes;	// if this is drawing color, it's color(ex.0xFFFFFF). otherwise it's resid
	boolean isDrawedColor;	// check if it's drawed color
	private Double timeRatio;

	public static final String Y_OFFSET = "yOffset";
	public static final String TEXT = "Text";
	public static final String COLOR_RES = "ColorRes";
	public static final String IS_DRAWED_COLOR = "IsDrawedColor";
	public static final String TIME_RATIO = "TimeRation";
	public TagObject(Double yOffset, String text, int color){
		this.setyOffset(yOffset);
		this.setText(text);
		this.setColorRes(color);
		this.isDrawedColor = false;
	}
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public Double getyOffset() {
		return yOffset;
	}
	public void setyOffset(Double yOffset) {
		this.yOffset = yOffset;
	}
	public int getColorRes() {
		return colorRes;
	}
	public void setColorRes(int colorRes) {
		this.colorRes = colorRes;
	}
	public Double getTimeRatio() {
		return timeRatio;
	}
	public void setTimeRatio(Double timeInMs) {
		this.timeRatio = timeInMs;
	}
	
	public JSONObject toJSONObject() throws JSONException{
		JSONObject obj = new JSONObject();
		obj.put(Y_OFFSET, yOffset);
		if(text == null)
			text = "";
		obj.put(TEXT, text);
		obj.put(COLOR_RES, colorRes);
		obj.put(IS_DRAWED_COLOR, isDrawedColor);
		obj.put(TIME_RATIO, timeRatio);
		return obj;
	}
	
	public TagObject toTagObject(JSONObject obj) throws JSONException{
		yOffset = obj.getDouble(Y_OFFSET);
		text = obj.getString(TEXT);
		colorRes = obj.getInt(COLOR_RES);
		isDrawedColor = obj.getBoolean(IS_DRAWED_COLOR);
		timeRatio = obj.getDouble(TIME_RATIO);
		return this;
	}
}
