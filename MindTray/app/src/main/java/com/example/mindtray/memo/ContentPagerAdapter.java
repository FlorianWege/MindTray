package com.example.mindtray.memo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

public class ContentPagerAdapter extends FragmentStatePagerAdapter {
	private Memo _memo;
	
	private Map<Content, ContentFragment> _fragmentsMap = new HashMap<Content, ContentFragment>();
	private Map<ContentFragment, Content> _fragmentsMapInv = new HashMap<ContentFragment, Content>();
	private NewMemoContentFragment _newMemoContentFragment = null;

	/*public interface OnDataSetChangedListener {
		void handle();
	}
	
	private List<OnDataSetChangedListener> _onDataSetChangedListeners = new ArrayList<OnDataSetChangedListener>();
	
	public void addOnDataSetChangedListener(OnDataSetChangedListener val) {
		_onDataSetChangedListeners.add(val);
	}*/
	
	public ContentPagerAdapter(FragmentManager fm, Memo memo) {
		super(fm);
		
		_memo = memo;
		
		/*_newMemoContentFragment = new NewMemoContentFragment();

		_newMemoContentFragment.setArgs(_memo);*/
		
		/*_memo.addListener(new Memo.Listener() {
			@Override
			public void contentAdded(Content content, boolean added, int pos) {
				if (added) {
					ContentFragment fragment = new ContentFragment();

					fragment.setArgs(content);
					
					_fragmentsMap.put(content, fragment);
					_fragmentsMapInv.put(fragment, content);
				} else {
					ContentFragment fragment = _fragmentsMap.get(content);
					
					_fragmentsMap.remove(content);
					_fragmentsMapInv.remove(fragment);
				}
				
				notifyDataSetChanged();
				
				for (OnDataSetChangedListener listener : _onDataSetChangedListeners) {
					listener.handle();
				}
			}
		});*/
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

	private Map<Content, ContentFragment> _contentMap = new HashMap<>();

	@Override
	public Fragment getItem(int index) {
		if (index >= _memo.getContents().size()) {
			if (_newMemoContentFragment == null) {
				_newMemoContentFragment = new NewMemoContentFragment();

				_newMemoContentFragment.setArgs(_memo);
			}

			return _newMemoContentFragment;
		}
		
		Content content = _memo.getContents().get(index);

		if (!_contentMap.containsKey(content)) {
			ContentFragment fragment = null;

			if (content instanceof TextContent) {
				TextContentFragment textContentFragment = new TextContentFragment();

				textContentFragment.setArgs((TextContent) content);

				fragment = textContentFragment;
			} else if (content instanceof ImageContent) {
				ImageContentFragment imageContentFragment = new ImageContentFragment();

				imageContentFragment.setArgs((ImageContent) content);

				fragment = imageContentFragment;
			} else if (content instanceof AudioContent) {
				AudioContentFragment audioContentFragment = new AudioContentFragment();

				audioContentFragment.setArgs((AudioContent) content);

				fragment = audioContentFragment;
			}

			if (fragment == null) {
				fragment = new ContentFragment();
			} else {
				/*ContentFragment innerFragment = fragment;

				fragment = new ContentFragment();

				fragment.setInnerFragment(innerFragment);*/
			}

			_contentMap.put(content, fragment);
		}

		return _contentMap.get(content);
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