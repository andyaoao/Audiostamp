package com.wang.audiostamp.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.wang.audiostamp.R;
import com.wang.audiostamp.R.drawable;
import com.wang.audiostamp.application.RecordApplication;
import com.wang.audiostamp.object.BallMapper;
import com.wang.audiostamp.object.TagObject;
import com.wang.audiostamp.object.WaveObject;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.DashPathEffect;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
public class AudioDisplayView extends View {

	public interface WaveformListener {
		public void waveformTouchStart(float x);
        public void waveformTouchMove(float x);
        public void waveformTouchEnd();
        public void waveformFling(float x);
        public void waveformDraw();
        public void waveformZoomIn();
        public void waveformZoomOut();
    };

    // Colors
    private Paint mGridPaint;
    private Paint mSelectedLinePaint;
    private Paint mUnselectedLinePaint;
    private Paint mUnselectedBkgndLinePaint;
    private Paint mBorderLinePaint;
    private Paint mPlaybackLinePaint;
    private Paint mTimecodePaint;

    private WaveObject mSoundFile;
    private int[] mLenByZoomLevel;
    private double[][] mValuesByZoomLevel;
    private double[] mZoomFactorByZoomLevel;
    private int[] mWidthsAtThisZoomLevel;
    private int mZoomLevel;
    private int mNumZoomLevels;
    private int mSampleRate;
    private int mSamplesPerFrame;
    int mOffset;
    private int mSelectionStart;
    private int mSelectionEnd;
    private int mPlaybackPos;
    private float mDensity;
    private float mInitialScaleSpan;
    private WaveformListener mListener;
    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;
    private boolean mInitialized;
    private int measuredHeight;
    private int measuredWidth;
    public boolean isViewing;
    //Random random;
    TagObject prevTag;
    public List<TagObject> tagList;
    private static final int ballSize = 32;
    private boolean prevFive; 
    // For the back record
    public int zeroBaseSecs = 0;
    public boolean isNegativeEnable = false;	
    public int maxZoomLength = 0;
    public AudioDisplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //if(random == null)
        //	random = new Random(System.currentTimeMillis());
        // We don't want keys, the markers get these
        setFocusable(false);

        mGridPaint = new Paint();
        mGridPaint.setAntiAlias(false);
        mGridPaint.setColor(
            getResources().getColor(R.drawable.grid_line));
        mSelectedLinePaint = new Paint();
        mSelectedLinePaint.setAntiAlias(false);
        mSelectedLinePaint.setColor(
            getResources().getColor(R.drawable.waveform_selected));
        mUnselectedLinePaint = new Paint();
        mUnselectedLinePaint.setAntiAlias(false);
        mUnselectedLinePaint.setColor(
            getResources().getColor(R.drawable.waveform_unselected));
        mUnselectedBkgndLinePaint = new Paint();
        mUnselectedBkgndLinePaint.setAntiAlias(false);
        mUnselectedBkgndLinePaint.setColor(
            getResources().getColor(
                R.drawable.waveform_unselected_bkgnd_overlay));
        mBorderLinePaint = new Paint();
        mBorderLinePaint.setAntiAlias(true);
        mBorderLinePaint.setStrokeWidth(5.5f);
        mBorderLinePaint.setPathEffect(
            new DashPathEffect(new float[] { 3.0f, 2.0f }, 0.0f));
        mBorderLinePaint.setColor(
            getResources().getColor(R.drawable.selection_border));
        mPlaybackLinePaint = new Paint();
        mPlaybackLinePaint.setAntiAlias(false);
        mPlaybackLinePaint.setColor(
            getResources().getColor(R.drawable.playback_indicator));
        mTimecodePaint = new Paint();
        mTimecodePaint.setTextSize(12);
        mTimecodePaint.setAntiAlias(true);
        mTimecodePaint.setColor(
            getResources().getColor(R.drawable.timecode));
        mTimecodePaint.setShadowLayer(
            2, 1, 1,
            getResources().getColor(R.drawable.timecode_shadow));

        mGestureDetector = new GestureDetector(
                context,
                new GestureDetector.SimpleOnGestureListener() {
                    public boolean onFling(
                                MotionEvent e1, MotionEvent e2, float vx, float vy) {
                        //mListener.waveformFling(vx);
                        return true;
                    }
                });

        mScaleGestureDetector = new ScaleGestureDetector(
                context,
                new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                    public boolean onScaleBegin(ScaleGestureDetector d) {
                        Log.i("Ringdroid", "ScaleBegin " + d.getCurrentSpanX());
                        mInitialScaleSpan = Math.abs(d.getCurrentSpanX());
                        return true;
                    }
                    public boolean onScale(ScaleGestureDetector d) {
                        float scale = Math.abs(d.getCurrentSpanX());
                        Log.i("Ringdroid", "Scale " + (scale - mInitialScaleSpan));
                        if (scale - mInitialScaleSpan > 40) {
                            mListener.waveformZoomIn();
                            mInitialScaleSpan = scale;
                        }
                        if (scale - mInitialScaleSpan < -40) {
                            mListener.waveformZoomOut();
                            mInitialScaleSpan = scale;
                        }
                        return true;
                    }
                    public void onScaleEnd(ScaleGestureDetector d) {
                        Log.i("Ringdroid", "ScaleEnd " + d.getCurrentSpanX());
                    }
                });

        mSoundFile = null;
        mLenByZoomLevel = null;
        mValuesByZoomLevel = null;
        mWidthsAtThisZoomLevel = null;
        mOffset = 0;
        mPlaybackPos = -1;
        mSelectionStart = 0;
        mSelectionEnd = 0;
        mDensity = 1.0f;
        mInitialized = false;
        //ballPainter = new Paint();
        BallMapper ballMapper = BallMapper.getInstance();
        ballMapper.buildColorMap((RecordApplication) AudioDisplayView.this.getContext().getApplicationContext());
        setBackgroundResource(R.color.trans);
    }
    
    public void resetValue(boolean isFull){
    	mSoundFile = null;
        mLenByZoomLevel = null;
        mValuesByZoomLevel = null;
        mWidthsAtThisZoomLevel = null;
        mOffset = 0;
        mPlaybackPos = -1;
        mSelectionStart = 0;
        mSelectionEnd = 0;
        mDensity = 1.0f;
        mInitialized = false;
        if(tagList != null){
        	if(isFull)
        		tagList.clear();
        	tagList = null;
        }
        isNegativeEnable = false;
        zeroBaseSecs = 0;
    }
    
    public void setNegativeTimeOffset(boolean isNeg, int offset){
    	isNegativeEnable = isNeg;
    	zeroBaseSecs = offset;
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mScaleGestureDetector.onTouchEvent(event);
        
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        }
        
        switch(event.getAction()) {
        case MotionEvent.ACTION_DOWN:
        	if(isViewing){
        		mListener.waveformTouchStart(event.getY());
        	}
            break;
        case MotionEvent.ACTION_MOVE:
        	if(isViewing){
        		mListener.waveformTouchMove(event.getY());
        	}
            break;
        case MotionEvent.ACTION_UP:
        	if(isViewing){
        		mListener.waveformTouchEnd();
        	}
            RecordApplication myApp = ((RecordApplication)this.getContext().getApplicationContext());
            EditText text = myApp.getTextField();
            float xValue = event.getX();
            float yValue = event.getY();
            
            if(prevTag != null){
            	prevTag.setText(text.getEditableText().toString());
            }
            text.setEnabled(false);
            text.setVisibility(View.GONE);            
            prevTag = null;
            stampSound(xValue, yValue);
            
            break;
        }
        return true;
    }
    
    public boolean stampSound(float xValue, float yValue){
    	if(xValue>(measuredWidth>>1)-20 && xValue<(measuredWidth>>1)+20){
    		RecordApplication myApp = ((RecordApplication)this.getContext().getApplicationContext());
            EditText text = myApp.getTextField();
            if(mOffset == 0){
    			int toTop = measuredHeight-maxZoomLength;
    			if(yValue<toTop)
    				return false;
    			yValue = ((yValue-toTop)/(measuredHeight-toTop))* maxZoomLength;
    		}
    		for(int i=0; tagList!=null&&i<tagList.size(); i++){
        		TagObject tag = tagList.get(i);
        		if(yValue>(tag.getyOffset()-mOffset)-40 && yValue<(tag.getyOffset()-mOffset)+40){
        			//text.setFocusable(true);
                	//text.setFocusableInTouchMode(true);
        			if(tag.getText()!=null && tag.getText().length() > 0)
        				text.setText(tag.getText());
        			else 
        				text.setHint("Fill Me");
        			text.setX(measuredWidth>>1);
                    text.setY(yValue);
                    text.setEnabled(true);
                    text.setVisibility(View.VISIBLE);
                    tag.setText("");
                    prevTag = tag;
        			return true;
        		}
        	}
        	//text.setHint("Fill Me");
        	text.setText("");
        	//text.setFocusable(true);
        	//text.setFocusableInTouchMode(true);
        	text.setVisibility(View.VISIBLE);
            text.setX(30+measuredWidth>>1);
            text.setY(yValue);
            text.setEnabled(true);
        	if(tagList == null)
        		tagList = new ArrayList<TagObject>();
        	BallMapper ballMapper = BallMapper.getInstance();
        	//long color = random.nextLong()%0xffffff;
        	int color = ballMapper.rotateColor();
        	text.setBackgroundColor((int) 0);
        	//Paint paint = new Paint();        			
			//paint.setColor((int) color);
			prevTag = new TagObject((double) (mOffset+yValue), "", color);
        	tagList.add(prevTag);
        	invalidate();
        }
		return false;	
    }
    
    public boolean hasSoundFile() {
        return mSoundFile != null;
    }

    public void setSoundFile(WaveObject soundFile) {
        mSoundFile = soundFile;
        mSampleRate = mSoundFile.getSampleRate();
        mSamplesPerFrame = mSoundFile.getSamplesPerFrame();
        computeDoublesForAllZoomLevels();
        mWidthsAtThisZoomLevel = null;
    }
    
    public void setTagList(List<TagObject> tagList){
    	if(tagList != null){
    		computeIntsForThisZoomLevel();
    		for(int i=0; i<tagList.size(); i++){
    			TagObject tag = tagList.get(i);
    			tag.setyOffset(mWidthsAtThisZoomLevel.length * tag.getTimeRatio());
    		}
    	}
    	this.tagList = tagList;
    }
    
    public void setWaveformHeight(int height){
    	measuredHeight = height;
    }
    
    public boolean isInitialized() {
        return mInitialized;
    }

    public int getZoomLevel() {
        return mZoomLevel;
    }

    public void setZoomLevel(int zoomLevel) {
        while (mZoomLevel > zoomLevel) {
            zoomIn();
        }
        while (mZoomLevel < zoomLevel) {
            zoomOut();
        }
    }

    public boolean canZoomIn() {
        return (mZoomLevel > 0);
    }

    public void zoomIn() {
        if (canZoomIn()) {
            mZoomLevel--;
            mSelectionStart *= 2;
            mSelectionEnd *= 2;
            mWidthsAtThisZoomLevel = null;
            int offsetCenter = mOffset + getMeasuredHeight() / 2;
            offsetCenter *= 2;
            mOffset = offsetCenter - getMeasuredHeight() / 2;
            if (mOffset < 0)
                mOffset = 0;
            invalidate();
        }
    }

    public boolean canZoomOut() {
        return (mZoomLevel < mNumZoomLevels - 1);
    }

    public void zoomOut() {
        if (canZoomOut()) {
            mZoomLevel++;
            mSelectionStart /= 2;
            mSelectionEnd /= 2;
            int offsetCenter = mOffset + getMeasuredHeight() / 2;
            offsetCenter /= 2;
            mOffset = offsetCenter - getMeasuredHeight() / 2;
            if (mOffset < 0)
                mOffset = 0;
            mWidthsAtThisZoomLevel = null;
            invalidate();
        }
    }

    public int maxPos() {
        return mLenByZoomLevel[mZoomLevel]-measuredHeight;
    }

    public int secondsToFrames(double seconds) {
        return (int)(1.0 * seconds * mSampleRate / mSamplesPerFrame + 0.5);
    }

    public int secondsToPixels(double seconds) {
        double z = mZoomFactorByZoomLevel[mZoomLevel];
        return (int)(z * seconds * mSampleRate / mSamplesPerFrame + 0.5);
    }

    public double pixelsToSeconds(int pixels) {
        double z = mZoomFactorByZoomLevel[mZoomLevel];
        return (pixels * (double)mSamplesPerFrame / (mSampleRate * z));
    }

    public int millisecsToPixels(int msecs) {
        double z = mZoomFactorByZoomLevel[mZoomLevel];
        return (int)((msecs * 1.0 * mSampleRate * z) /
                     (1000.0 * mSamplesPerFrame) + 0.5);
    }

    public int pixelsToMillisecs(int pixels) {
        double z = mZoomFactorByZoomLevel[mZoomLevel];
        return (int)(pixels * (1000.0 * mSamplesPerFrame) /
                     (mSampleRate * z) + 0.5);
    }

    public void waveformDraw(int start, int end, int offset) {
        mSelectionStart = start;
        mSelectionEnd = end;
        mOffset = offset;
    }

    public int getStart() {
        return mSelectionStart;
    }

    public int getEnd() {
        return mSelectionEnd;
    }

    public int getOffset() {
        return mOffset;
    }

    public void setPlayback(int pos) {
        mPlaybackPos = pos;
    }

    public void setListener(WaveformListener listener) {
        mListener = listener;
    }
    
    public WaveformListener getListener() {
        return mListener;
    }
    
    public void recomputeHeights(float density) {
        mWidthsAtThisZoomLevel = null;
        mDensity = density;
        mTimecodePaint.setTextSize((int)(12 * density));

        invalidate();
    }

    protected void drawWaveformLine(Canvas canvas,
                                    int y, int x0, int x1,		// x, yo, y1 -> y, x0, x1
                                    Paint paint) {
    	
    	canvas.drawLine(x0, y, x1, y, paint);
    }

    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (mSoundFile == null)
            return;

        if (mWidthsAtThisZoomLevel == null)
            computeIntsForThisZoomLevel();
        
        // Draw waveform
        measuredWidth = getMeasuredWidth();
        //measuredHeight = getMeasuredHeight();
        if( !isViewing && mWidthsAtThisZoomLevel.length>measuredHeight)
        	mOffset = mWidthsAtThisZoomLevel.length - measuredHeight;
        
        int ctr = measuredWidth / 2;
        int toBottomY;        

        // Draw grid
        double onePixelInSecs = pixelsToSeconds(1);
        boolean onlyEveryFiveSecs = (onePixelInSecs > 1.0 / 50.0);
        if(onlyEveryFiveSecs!=prevFive)
        	mOffset = 0;
        
        int start = mOffset;
        int height = mWidthsAtThisZoomLevel.length - start;
        if (height > measuredHeight)
        	height = measuredHeight;
        maxZoomLength = mWidthsAtThisZoomLevel.length;
        double fractionalSecs = mOffset * onePixelInSecs;
        prevFive = onlyEveryFiveSecs;
        int integerSecs = (int) fractionalSecs;
        int i = height;
        int reminder = zeroBaseSecs%5;
        while (i > 0) {
            i--;
            fractionalSecs += onePixelInSecs;
            int integerSecsNew = (int) fractionalSecs;
            if (integerSecsNew != integerSecs) {
                integerSecs = integerSecsNew;
                int test = 0;
                if(fractionalSecs>=reminder)
                	test = reminder;
                if (!onlyEveryFiveSecs || 0 == ((integerSecs-test) % 5)) {
                    canvas.drawLine(0, measuredHeight-i, measuredWidth, measuredHeight-i, mGridPaint);
                }
            }
        }
        
        toBottomY = measuredHeight - height;
        fractionalSecs = mOffset * onePixelInSecs;
        prevFive = onlyEveryFiveSecs;
        integerSecs = (int) fractionalSecs;
        // Draw waveform
        for (i = 0; i < height ; i++) {
            Paint paint;
            fractionalSecs += onePixelInSecs;
            /*if (i + start >= mSelectionStart &&
                i + start < mSelectionEnd) {
                paint = mSelectedLinePaint;
            } else {
                drawWaveformLine(canvas, measuredHeight-i, 0, measuredWidth, mUnselectedBkgndLinePaint);
                paint = mUnselectedLinePaint;
            }
            */
            if(fractionalSecs < zeroBaseSecs){
            	paint = mSelectedLinePaint;
            } else {
            	paint = mUnselectedLinePaint;
            }
            if(start+i > 0){
            drawWaveformLine(
                    canvas, i+toBottomY,
                    ctr - mWidthsAtThisZoomLevel[(start + i)],
                    ctr + 1 + mWidthsAtThisZoomLevel[(start + i)],
                    paint);
            }
            if (i + start == mPlaybackPos) {
                canvas.drawLine(0, measuredHeight-i, measuredWidth, measuredHeight-i, mPlaybackLinePaint);
            }
        }

        // If we can see the right edge of the waveform, draw the
        // non-waveform area to the right as unselected
        for (i = height; i < measuredHeight; i++) {
            drawWaveformLine(canvas, measuredHeight-i, 0, measuredWidth, mUnselectedBkgndLinePaint);            
        }

        // Draw borders
        canvas.drawLine(
                0,
                measuredHeight, measuredWidth, measuredHeight, 
                mBorderLinePaint);        
        
        /*canvas.drawLine(
            30,
            measuredHeight-(mSelectionStart - mOffset + 0.5f), measuredWidth, measuredHeight-(mSelectionStart - mOffset + 0.5f), 
            mBorderLinePaint);
        canvas.drawLine(
            0,
            measuredHeight-(mSelectionEnd - mOffset + 0.5f), measuredWidth - 30, measuredHeight-(mSelectionEnd - mOffset + 0.5f), 
            mBorderLinePaint);
         */
        
        // Draw timecode
        double timecodeIntervalSecs = 5.0;
        
        if (timecodeIntervalSecs / onePixelInSecs < 50) {
            timecodeIntervalSecs = 5.0;
        }
        //if (timecodeIntervalSecs / onePixelInSecs < 50) {
        //    timecodeIntervalSecs = 15.0;
        //}

        // Draw grid
        fractionalSecs = mOffset * onePixelInSecs;
        int integerTimecode = (int) (fractionalSecs / timecodeIntervalSecs);
        i = height;
        
        while (i >0 ) {
            i--;
            fractionalSecs += onePixelInSecs;
            integerSecs = (int) fractionalSecs;
            //if((fractionalSecs-reminder)%timecodeIntervalSecs==0)
            //	fractionalSecs = ((fractionalSecs-reminder)+timecodeIntervalSecs);
            int test = 0;
            if(fractionalSecs>=reminder)
            	test = reminder;
            int integerTimecodeNew = (int) ((fractionalSecs-test) /
                                            timecodeIntervalSecs);
            if (integerTimecodeNew != integerTimecode) {
                integerTimecode = integerTimecodeNew;

                // Turn, e.g. 67 seconds into "1:07"
                String timecodeMinutes = "0";
                String timecodeSeconds = "0";
                if(isNegativeEnable){
                	if(integerSecs < zeroBaseSecs){
                		timecodeMinutes = "-" + (Math.abs(integerSecs-zeroBaseSecs) / 60);
                		mTimecodePaint.setColor(mSelectedLinePaint.getColor());
                	} else {
                		timecodeMinutes = "+" + ((integerSecs-zeroBaseSecs) / 60);
                		mTimecodePaint.setColor(mUnselectedLinePaint.getColor());
                	}
                	timecodeSeconds = "" + (Math.abs(integerSecs-zeroBaseSecs) % 60);
                } else {
                	timecodeMinutes = "" + (integerSecs / 60);
                	timecodeSeconds = "" + (integerSecs % 60);
                }
                if ((Math.abs(integerSecs-zeroBaseSecs) % 60) < 10) {
                    timecodeSeconds = "0" + timecodeSeconds;
                }
                String timecodeStr = timecodeMinutes + ":" + timecodeSeconds;
                float offset = (float) (
                    0.5 * mTimecodePaint.measureText(timecodeStr));
                canvas.drawText(timecodeStr,
                                (int)(12 * mDensity),
                                measuredHeight-(i - offset),
                                mTimecodePaint);
            }
        }
        if(tagList!=null){
        	for(i=0; i<tagList.size(); i++){
        		if(tagList.get(i).getyOffset()>mOffset && tagList.get(i).getyOffset()<mOffset+measuredHeight)
        		{   
        			//canvas.drawCircle(measuredWidth>>1, (float) (yIndex.get(i).yOffset-mOffset), 16, yIndex.get(i).color);
        			Bitmap bm = zoomBitmap(BallMapper.getColorResId(tagList.get(i).getColorRes()), ballSize, ballSize);
        			int baseFiller = 0;
        			if(mOffset == 0){
        				baseFiller = measuredHeight - mWidthsAtThisZoomLevel.length ;
        			}
        			canvas.drawBitmap(bm, (measuredWidth>>1)-(ballSize>>1), (float) (baseFiller+tagList.get(i).getyOffset()-mOffset), null);	
        			/*ballPainter.setXfermode(new PorterDuffXfermode(
          		          android.graphics.PorterDuff.Mode.MULTIPLY));
        			*/
        			if(tagList.get(i).getText()!=null)
        				canvas.drawText(tagList.get(i).getText(),
                            (int)((measuredWidth>>1)+20),
                            (float) (baseFiller+tagList.get(i).getyOffset()-mOffset),
                            mTimecodePaint);
        		}
        	}
        }
        /*if (mListener != null) {
            mListener.waveformDraw();
        }*/
    }
    public Bitmap zoomBitmap(int resId, int w,int h){
    	Resources res= getResources();
		Bitmap bitmap = BitmapFactory.decodeResource(res, resId);		
    	int width = bitmap.getWidth();
    	int height = bitmap.getHeight();
    	Matrix matrix = new Matrix();
    	float scaleWidht = ((float)w / width);
    	float scaleHeight = ((float)h / height);
    	matrix.postScale(scaleWidht, scaleHeight);
    	Bitmap newbmp = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    	return newbmp;
    }
    
    /**
     * Called once when a new sound file is added
     */
    private void computeDoublesForAllZoomLevels() {
        int numFrames = mSoundFile.getNumFrames();
        int[] frameGains = mSoundFile.getFrameGains();
        double[] smoothedGains = new double[numFrames];
        if (numFrames == 1) {
            smoothedGains[0] = frameGains[0];
        } else if (numFrames == 2) {
            smoothedGains[0] = frameGains[0];
            smoothedGains[1] = frameGains[1];
        } else if (numFrames > 2) {
            smoothedGains[0] = (double)(
                (frameGains[0] / 2.0) +
                (frameGains[1] / 2.0));
            for (int i = 1; i < numFrames - 1; i++) {
                smoothedGains[i] = (double)(
                    (frameGains[i - 1] / 3.0) +
                    (frameGains[i    ] / 3.0) +
                    (frameGains[i + 1] / 3.0));
            }
            smoothedGains[numFrames - 1] = (double)(
                (frameGains[numFrames - 2] / 2.0) +
                (frameGains[numFrames - 1] / 2.0));
        }

        // Make sure the range is no more than 0 - 255
        double maxGain = 1.0;
        for (int i = 0; i < numFrames; i++) {
            if (smoothedGains[i] > maxGain) {
                maxGain = smoothedGains[i];
            }
        }
        double scaleFactor = 1.0;
        if (maxGain > 255.0) {
            scaleFactor = 255 / maxGain;
        }        

        // Build histogram of 256 bins and figure out the new scaled max
        maxGain = 0;
        int gainHist[] = new int[256];
        for (int i = 0; i < numFrames; i++) {
            int smoothedGain = (int)(smoothedGains[i] * scaleFactor);
            if (smoothedGain < 0)
                smoothedGain = 0;
            if (smoothedGain > 255)
                smoothedGain = 255;

            if (smoothedGain > maxGain)
                maxGain = smoothedGain;

            gainHist[smoothedGain]++;
        }

        // Re-calibrate the min to be 5%
        double minGain = 0;
        int sum = 0;
        while (minGain < 255 && sum < numFrames / 20) {
            sum += gainHist[(int)minGain];
            minGain++;
        }

        // Re-calibrate the max to be 99%
        sum = 0;
        while (maxGain > 2 && sum < numFrames / 100) {
            sum += gainHist[(int)maxGain];
            maxGain--;
        }

        // Compute the heights
        double[] heights = new double[numFrames];
        double range = maxGain - minGain;
        for (int i = 0; i < numFrames; i++) {
            double value = (smoothedGains[i] * scaleFactor - minGain) / range;
            if (value < 0.0)
                value = 0.0;
            if (value > 1.0)
                value = 1.0;
            heights[i] = value * value;
        }

        mNumZoomLevels = 5;
        mLenByZoomLevel = new int[5];
        mZoomFactorByZoomLevel = new double[5];
        mValuesByZoomLevel = new double[5][];

        // Level 0 is doubled, with interpolated values
        mLenByZoomLevel[0] = numFrames * 2;
        mZoomFactorByZoomLevel[0] = 2.0;
        mValuesByZoomLevel[0] = new double[mLenByZoomLevel[0]];
        if (numFrames > 0) {
            mValuesByZoomLevel[0][0] = 0.5 * heights[0];
            mValuesByZoomLevel[0][1] = heights[0];
        }
        for (int i = 1; i < numFrames; i++) {
            mValuesByZoomLevel[0][2 * i] = 0.5 * (heights[i - 1] + heights[i]);
            mValuesByZoomLevel[0][2 * i + 1] = heights[i];
        }

        // Level 1 is normal
        mLenByZoomLevel[1] = numFrames;
        mValuesByZoomLevel[1] = new double[mLenByZoomLevel[1]];
        mZoomFactorByZoomLevel[1] = 1.0;
        for (int i = 0; i < mLenByZoomLevel[1]; i++) {
            mValuesByZoomLevel[1][i] = heights[i];
        }

        // 3 more levels are each halved
        for (int j = 2; j < 5; j++) {
            mLenByZoomLevel[j] = mLenByZoomLevel[j - 1] / 2;
            mValuesByZoomLevel[j] = new double[mLenByZoomLevel[j]];
            mZoomFactorByZoomLevel[j] = mZoomFactorByZoomLevel[j - 1] / 2.0;
            for (int i = 0; i < mLenByZoomLevel[j]; i++) {
                mValuesByZoomLevel[j][i] =
                    0.5 * (mValuesByZoomLevel[j - 1][2 * i] +
                           mValuesByZoomLevel[j - 1][2 * i + 1]);
            }
        }

        /*if (numFrames > 5000) {
            mZoomLevel = 3;
        } else if (numFrames > 1000) {
            mZoomLevel = 2;
        } else if (numFrames > 300) {
            mZoomLevel = 1;
        } else {
            mZoomLevel = 0;
        }*/
        mZoomLevel = 3;

        mInitialized = true;
    }

    /**
     * Called the first time we need to draw when the zoom level has changed
     * or the screen is resized
     */
    private void computeIntsForThisZoomLevel() {
        int halfWidth = (getMeasuredWidth() / 2) - 1;
        mWidthsAtThisZoomLevel = new int[mLenByZoomLevel[mZoomLevel]];
        for (int i = 0; i < mLenByZoomLevel[mZoomLevel]; i++) {
        	mWidthsAtThisZoomLevel[i] =
                (int)(mValuesByZoomLevel[mZoomLevel][i] * halfWidth);
        }
    }
    
    public int getMaxLevel(){
    	return maxZoomLength;
    }
}
