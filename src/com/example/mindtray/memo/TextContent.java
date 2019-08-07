package com.example.mindtray.memo;

public class TextContent extends MemoContent {
	private String _val = null;
	
	public String getText() {
		return _val;
	}
	
	public void setText(String val) {
		_val = val;
	}
	
	public TextContent(String name) {
		super(name);
	}
}
