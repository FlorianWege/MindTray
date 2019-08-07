package com.example.mindtray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Memo {
	String _name;
	
	public String toString() {
		return _name;
	}
	
	List<MemoContent> _contents = new ArrayList<MemoContent>();
	
	public List<MemoContent> getContents() {
		return _contents;
	}
	
	Map<String, MemoContent> _contentsMap = new HashMap<String, MemoContent>();
	
	public MemoContent getContent(String name) {
		return _contentsMap.get(name);
	}
	
	public void addContent(MemoContent content) {
		_contents.add(content);
		_contentsMap.put(content.toString(), content);
	}
	
	public Memo(String name) {
		_name = name;
	}
}
