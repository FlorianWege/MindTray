package com.example.mindtray.memo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Memo implements Serializable {
	private String _name;
	
	@Override
	public String toString() {
		return _name;
	}
	
	private List<MemoContent> _contents = new ArrayList<MemoContent>();
	
	public List<MemoContent> getContents() {
		return new ArrayList<MemoContent>(_contents);
	}
	
	private Map<String, MemoContent> _contentsMap = new HashMap<String, MemoContent>();
	
	public MemoContent getContent(String name) {
		return _contentsMap.get(name);
	}
	
	public void addContent(final MemoContent val) {
		Log.e(getClass().getSimpleName(), this +";" + System.identityHashCode(this) + " AAAadd content size " + System.identityHashCode(_contents) + ";" + getContents().size());
		
		if (_contents.contains(val)) return;
		if (_contentsMap.containsKey(val.toString())) return;
		
		_contents.add(val);
		_contentsMap.put(val.toString(), val);
		
		val.addOnRemoveListener(new MemoContent.OnRemoveListener() {
			@Override
			public void handle() {
				removeContent(val);
			}
		});
		
		for (ContentAddedListener listener : _contentAddedListeners) {
			listener.handle(val, true, _contents.size() - 1);
		}
		
		Log.e(getClass().getSimpleName(), this + " BBBadd content size " + System.identityHashCode(_contents) + ";" + getContents().size());
	}
	
	public void removeContent(MemoContent val) {
		if (!_contents.contains(val)) return;
		
		int pos = _contents.indexOf(val);
		
		_contents.remove(val);
		_contentsMap.remove(val.toString());
		
		for (ContentAddedListener listener : _contentAddedListeners) {
			listener.handle(val, false, pos);
		}
		
		Log.e(getClass().getSimpleName(), this + " remove content size " + System.identityHashCode(_contents) + ";" + getContents().size());
	}
	
	public interface ContentAddedListener {
		public void handle(MemoContent content, boolean added, int pos);
	}
	
	private List<ContentAddedListener> _contentAddedListeners = new ArrayList<ContentAddedListener>();
	
	public void addContentAddedListener(ContentAddedListener val) {
		_contentAddedListeners.add(val);
	}
	
	public Memo(String name) {
		_name = name;
		
		Log.e(getClass().getSimpleName(), this +";" + System.identityHashCode(this) + " create " + System.identityHashCode(_contents));
	}
}
