package com.example.mindtray.memo;

import com.example.mindtray.shared.Storage;
import com.example.mindtray.shared.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/*
	model class for one memo
	contains:
		name
		date
		collection of Contents
 */

public class Memo {
	//user visible name
	private String _name;

	public String getName() {
		return _name;
	}

	public void setName(String val) {
		_name = val;
	}
	
	@Override
	public String toString() {
		return getName();
	}

	//date the memo is bound to
	private Calendar _date;

	public Calendar getDate() {
		return _date;
	}

	public void setDate(Calendar val) {
		_date = val;
	}

	//format the date nicely
	public String getDateS() {
		return new SimpleDateFormat("yyyy-MM-dd").format(_date.getTime());
	}

	//key for data base storage
	public String getKey() {
		return String.format("%s_%s", getName(), getDateS());
	}

	//contents
	private List<Content> _contents = new ArrayList<>();
	
	public List<Content> getContents() {
		return new ArrayList<>(_contents);
	}
	
	//private Map<String, Content> _contentsMap = new HashMap<>();
	
	public Content getContentByName(String name) {
		for (Content content : getContents()) {
			if (content.getName().equals(name)) return content;
		}

		return null;
	}
	
	public void addContent(final Content val) throws Storage.StorageException {
		if (_contents.contains(val)) return;
		//if (_contentsMap.containsKey(val.getKey())) return;

		Storage.getInstance(Util.getContext()).getDB().getMemoHandler(this).addContent(val);
		
		_contents.add(val);
		//_contentsMap.put(val.getKey(), val);

		//listen to the contained contents so we can update the storage
		val.addListener(new Content.Listener() {
			@Override
			public void onRemove() throws Storage.StorageException {
				removeContent(val);
			}

			@Override
			public void textChanged() throws Storage.StorageException {
				Storage.getInstance(Util.getContext()).getDB().getMemoHandler(Memo.this).updateContent(val);

				for (Listener listener : _listeners) {
					listener.contentChanged();
				}
			}
		});
		
		for (Listener listener : _listeners) {
			listener.contentAdded(val, true, _contents.size() - 1);
		}
	}
	
	public void removeContent(Content val) throws Storage.StorageException {
		if (!_contents.contains(val)) return;

		Storage.getInstance(Util.getContext()).getDB().getMemoHandler(this).removeContent(val);

		int pos = _contents.indexOf(val);
		
		_contents.remove(val);
		//_contentsMap.remove(val.toString());
		
		for (Listener listener : _listeners) {
			listener.contentAdded(val, false, pos);
		}
	}
	
	public interface Listener {
		void contentAdded(Content content, boolean added, int pos);
		void contentChanged();
	}
	
	private List<Listener> _listeners = new ArrayList<>();
	
	public void addListener(Listener val) {
		_listeners.add(val);
	}
	
	public Memo(String name, Calendar date) {
		_name = name;
		_date = date;
	}
}