package com.example.mindtray.memo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

public class MemoContentPagerAdapter extends FragmentStatePagerAdapter {
	private Memo _memo;
	
	private Map<MemoContent, MemoContentFragment> _fragmentsMap = new HashMap<MemoContent, MemoContentFragment>();
	private Map<MemoContentFragment, MemoContent> _fragmentsMapInv = new HashMap<MemoContentFragment, MemoContent>();
	private NewMemoContentFragment _newMemoContentFragment;

	public interface OnDataSetChangedListener {
		public void handle();
	}
	
	private List<OnDataSetChangedListener> _onDataSetChangedListeners = new ArrayList<OnDataSetChangedListener>();
	
	public void addOnDataSetChangedListener(OnDataSetChangedListener val) {
		_onDataSetChangedListeners.add(val);
	}
	
	public MemoContentPagerAdapter(FragmentManager fm, Memo memo) {
		super(fm);
		
		_memo = memo;
		
		_newMemoContentFragment = new NewMemoContentFragment(_memo);
		
		_memo.addContentAddedListener(new Memo.ContentAddedListener() {
			@Override
			public void handle(MemoContent content, boolean added, int pos) {
				if (added) {
					MemoContentFragment fragment = new MemoContentFragment(content);
					
					_fragmentsMap.put(content, fragment);
					_fragmentsMapInv.put(fragment, content);
				} else {
					MemoContentFragment fragment = _fragmentsMap.get(content);
					
					_fragmentsMap.remove(content);
					_fragmentsMapInv.remove(fragment);
				}
				
				notifyDataSetChanged();
				
				for (OnDataSetChangedListener listener : _onDataSetChangedListeners) {
					listener.handle();
				}
			}
		});
	}

	/*@Override
	public Fragment getItem(int index) {
		if (index >= _memo.getContents().size()) return _newMemoContentFragment;

		return _fragmentsMap.get(_memo.getContents().get(index));
	}
	
	@Override
	public int getItemPosition(Object val) {
		if (val instanceof NewMemoContentFragment) return _memo.getContents().size();
		
		int pos = _memo.getContents().indexOf(_fragmentsMapInv.get(val));
		
		if (pos == -1) pos = POSITION_NONE;
		
		return pos;
	}*/
	
	@Override
	public Fragment getItem(int index) {
		Log.e("getitem", new Integer(index).toString());
		if (index >= _memo.getContents().size()) return new NewMemoContentFragment(_memo);
		
		MemoContent content = _memo.getContents().get(index);

		MemoContentFragment fragment = new MemoContentFragment(content);
		
		if (content instanceof TextContent) {
		} else if (content instanceof ImageContent) {
			fragment = new ImageContentFragment((ImageContent) content);
		} else if (content instanceof AudioContent) {
			
		}

		return fragment;
	}
	
	@Override
	public int getItemPosition(Object val) {
		return POSITION_NONE;
		/*Log.d("getitempos", val.toString());
		if (val instanceof NewMemoContentFragment) return _memo.getContents().size();
		
		int pos = _memo.getContents().indexOf(val);
		
		if (pos == -1) pos = POSITION_NONE;
		
		return pos;*/
	}

	@Override
	public int getCount() {
		return _memo.getContents().size() + 1;
	}

}
