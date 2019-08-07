package com.example.mindtray;

public class MemoTextContent extends MemoContent {
	String _text;
	
	public String getText() {
		return _text;
	}
	
	public void setText(String text) {
		_text = text;
	}
	
	public MemoTextContent(String name) {
		super(name);
	}
}
