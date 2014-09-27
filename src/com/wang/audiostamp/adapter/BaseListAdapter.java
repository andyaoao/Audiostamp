package com.wang.audiostamp.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.BaseAdapter;

public abstract class BaseListAdapter<T> extends BaseAdapter
{
	private final static String TAG = BaseListAdapter.class.getSimpleName();
	
	protected ArrayList<T> list; //= new ArrayList<T>();
	protected Context mContext;
	
	protected int diWidth;
	protected int diHeight;
	protected float density;
	
	protected SharedPreferences sharedPref;
	
	public BaseListAdapter(Context context, ArrayList<T> list) 
	{
		this.list = list;
		this.mContext = context;

		sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		
		density =  context.getResources().getDisplayMetrics().density;		
		diWidth = context.getResources().getDisplayMetrics().widthPixels;
		diHeight = context.getResources().getDisplayMetrics().heightPixels;
	}

	@Override
	public int getCount() 
	{
		return (list == null) ? 0 : list.size();
	}

	@Override
	public T getItem(int position) 
	{
		return list.get(position);
	}

	@Override
	public long getItemId(int position) 
	{
		return position;
	}

	@Override
	public boolean hasStableIds() 
	{
		return true;
	}

	@Override
	public boolean isEmpty() 
	{
		return getCount() == 0;
	}
	
	public boolean addAll(List<? extends T> _list) 
	{
		return list.addAll(_list);
	}

	public boolean addOne(T object)
	{
		return list.add(object);
	}

	public void addOne(int location, T object)
	{
		list.add(location, object);
	}
	
	public List<? extends T> getAllList()
	{
		return list;
	}
/**
 * @see java.util.List#clear()
 */
	public void clear() 
	{
		list.clear();
	}

/**
 * @param location
 * @return
 * @see java.util.List#remove(int)
 */
	public T remove(int location) 
	{
		return list.remove(location);
	}
}
