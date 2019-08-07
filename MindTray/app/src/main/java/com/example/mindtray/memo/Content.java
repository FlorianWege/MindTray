package com.example.mindtray.memo;

import com.example.mindtray.shared.Storage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Content {
	private String _name;

	public String getName() {
		return _name;
	}

	public String getKey() {
		return "key_" + getName();
	}

	@Override
	public String toString() {
		return getName();
	}
	
	public interface Listener {
		void onRemove() throws Storage.StorageException;
		void textChanged() throws Storage.StorageException;
	}
	
	private List<Listener> _listeners = new ArrayList<>();
	
	public void addListener(Listener val) {
		_listeners.add(val);
	}
	
	public void remove() throws Storage.StorageException {
		for (Listener listener : _listeners) {
			listener.onRemove();
		}
	}

	private String _text = "";

	public String getText() {
		return _text;
	}

	public void setText(String val) throws Storage.StorageException {
		_text = val;

		for (Listener listener : _listeners) {
			listener.textChanged();
		}
	}

	public Content(String name) {
		_name = name;
	}
}