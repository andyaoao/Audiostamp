package com.wang.audiostamp.view;


public class AudioTouchHandler implements AudioDisplayView.WaveformListener {
	private AudioDisplayView mWaveformView;
	int mWidth;
	int mOffsetGoal;
	//int mOffset;
	boolean mKeyDown;
	boolean mTouchDragging;
	int mStartPos;
	int mEndPos;
	public int mMaxPos;
	boolean mIsPlaying;
	int mFlingVelocity;
	float mTouchStart;
	int mTouchInitialOffset;
	long mWaveformTouchStartMsec;
	
    /**
     * Every time we get a message that our waveform drew, see if we need to
     * animate and trigger another redraw.
     */
	
	public AudioTouchHandler(AudioDisplayView waveformView){
		mWaveformView = waveformView;
	}
	
    public void waveformDraw() {
        mWidth = mWaveformView.getMeasuredWidth();
        if (mOffsetGoal != mWaveformView.mOffset && !mKeyDown)
            updateDisplay();
        else if (mIsPlaying) {
            updateDisplay();
        } else if (mFlingVelocity != 0) {
            updateDisplay();
        }
    }

    public void waveformTouchStart(float x) {
        mTouchDragging = true;
        mTouchStart = x;
        mTouchInitialOffset = mWaveformView.mOffset;
        mFlingVelocity = 0;
        mWaveformTouchStartMsec = System.currentTimeMillis();
    }
    
    private int trap(int pos) {
        if (pos < 1)
            return 0;
        if (pos > mMaxPos)
            return mMaxPos;
        
        return pos;
    }

    public void waveformTouchMove(float x) {
    	mWaveformView.mOffset = trap((int)(mTouchInitialOffset + (mTouchStart - x)));
        updateDisplay();
    }

    public void waveformTouchEnd() {
        mTouchDragging = false;
        mOffsetGoal = mWaveformView.mOffset;

        long elapsedMsec = System.currentTimeMillis() -
            mWaveformTouchStartMsec;
        
        /*if (elapsedMsec < 300) {
            if (mIsPlaying) {
                int seekMsec = mWaveformView.pixelsToMillisecs(
                    (int)(mTouchStart + mOffset));
                if (seekMsec >= mPlayStartMsec &&
                    seekMsec < mPlayEndMsec) {
                    mPlayer.seekTo(seekMsec - mPlayStartOffset);
                } else {
                    handlePause();
                }
            } else {
                onPlay((int)(mTouchStart + mOffset));
            }
        }*/
    }

    public void waveformFling(float vx) {
        mTouchDragging = false;
        mOffsetGoal = mWaveformView.mOffset;
        mFlingVelocity = (int)(-vx);
        updateDisplay();
    }

    public void waveformZoomIn() {
        mWaveformView.zoomIn();
        mStartPos = mWaveformView.getStart();
        mEndPos = mWaveformView.getEnd();
        mMaxPos = mWaveformView.maxPos();
        mWaveformView.mOffset = mWaveformView.getOffset();
        mOffsetGoal = mWaveformView.mOffset;
        //enableZoomButtons();
        updateDisplay();
    }

    public void waveformZoomOut() {
        mWaveformView.zoomOut();
        mStartPos = mWaveformView.getStart();
        mEndPos = mWaveformView.getEnd();
        mMaxPos = mWaveformView.maxPos();
        mWaveformView.mOffset = mWaveformView.getOffset();
        mOffsetGoal = mWaveformView.mOffset;
        //enableZoomButtons();
        updateDisplay();
    }
    private synchronized void updateDisplay() {
    	/*if (mIsPlaying) {
            int now = mPlayer.getCurrentPosition() + mPlayStartOffset;
            int frames = mWaveformView.millisecsToPixels(now);
            mWaveformView.setPlayback(frames);
            setOffsetGoalNoUpdate(frames - mWidth / 2);
            if (now >= mPlayEndMsec) {
                handlePause();
            }
        }*/

        if (!mTouchDragging) {
            int offsetDelta;

            if (mFlingVelocity != 0) {
                float saveVel = mFlingVelocity;

                offsetDelta = mFlingVelocity / 30;
                if (mFlingVelocity > 80) {
                    mFlingVelocity -= 80;
                } else if (mFlingVelocity < -80) {
                    mFlingVelocity += 80;
                } else {
                    mFlingVelocity = 0;
                }

                mWaveformView.mOffset += offsetDelta;

                if (mWaveformView.mOffset + mWidth / 2 > mMaxPos) {
                	mWaveformView.mOffset = mMaxPos - mWidth / 2;
                    mFlingVelocity = 0;
                }
                if (mWaveformView.mOffset < 0) {
                	mWaveformView.mOffset = 0;
                    mFlingVelocity = 0;
                }
                mOffsetGoal = mWaveformView.mOffset;
            } else {
                offsetDelta = mOffsetGoal - mWaveformView.mOffset;

                if (offsetDelta > 10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta > 0)
                    offsetDelta = 1;
                else if (offsetDelta < -10)
                    offsetDelta = offsetDelta / 10;
                else if (offsetDelta < 0)
                    offsetDelta = -1;
                else
                    offsetDelta = 0;

                mWaveformView.mOffset += offsetDelta;
            }
        }

    	mWaveformView.invalidate();
    }
}
