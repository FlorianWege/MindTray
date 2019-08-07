package com.example.mindtray.memo;

import java.util.ArrayList;
import java.util.List;

public abstract class MemoContent {
	private String _name;
	
	@Override
	public String toString() {
		return _name;
	}
	
	public interface OnRemoveListener {
		public void handle();
	}
	
	private List<OnRemoveListener> _onRemoveListeners = new ArrayList<OnRemoveListener>();
	
	public void addOnRemoveListener(OnRemoveListener val) {
		_onRemoveListeners.add(val);
	}
	
	public void remove() {
		for (OnRemoveListener listener : _onRemoveListeners) {
			listener.handle();
		}
	}
	
	public MemoContent(String name) {
		_name = name;
	}
}
