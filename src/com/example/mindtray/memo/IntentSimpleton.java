package com.example.mindtray.memo;

import java.util.HashMap;
import java.util.Map;

public class IntentSimpleton {
	private Map<String, Object> _vals = new HashMap<String, Object>();
	
	public Object get(String key) {
		return _vals.get(key);
	}
	
	public void set(String key, Object val) {
		_vals.put(key, val);
	}
	
	private static IntentSimpleton _instance = new IntentSimpleton();
	
	public static IntentSimpleton getInstance() {
		return _instance;
	}
	
	private IntentSimpleton() {
	}
}
