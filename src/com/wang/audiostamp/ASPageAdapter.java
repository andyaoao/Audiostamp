package com.wang.audiostamp;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.viewpagerindicator.IconPagerAdapter;
import com.wang.audiostamp.R;
import com.wang.audiostamp.R.drawable;
import com.wang.audiostamp.fragment.PlayFragment;
import com.wang.audiostamp.fragment.RecordFragment;

public class ASPageAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
    protected static final String[] CONTENT = new String[] { "RECORD", "PLAY"};
    protected static final int[] ICONS = new int[] {
            R.drawable.perm_record_page,
            R.drawable.perm_play_page,
    };

    private int mCount = CONTENT.length;

    public ASPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
    	if(position == 0)
    		return RecordFragment.getInstance();
    	else if(position == 1)
    		return PlayFragment.getInstance();
    	else 
    		return null;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return ASPageAdapter.CONTENT[position % CONTENT.length];
    }

    @Override
    public int getIconResId(int index) {
      return ICONS[index % ICONS.length];
    }

    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = count;
            notifyDataSetChanged();
        }
    }
}
