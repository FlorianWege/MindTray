package com.example.mindtray.memo;

import com.example.mindtray.shared.Storage;

public class TextContent extends Content {
	private String _val = null;

	//TODO: TextContent overrides get/setText of superclass Content, maybe unnecessarily...
	public String getText() {
		return _val;
	}
	
	public void setText(String val) throws Storage.StorageException {
		super.setText(val);

		_val = val;
	}
	
	public TextContent(String name, String text) throws Storage.StorageException {
		super(name);

		setText(text);
	}
}