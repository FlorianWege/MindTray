package com.example.mindtray.shared;

import java.util.HashMap;
import java.util.Map;

/*
	passing data between activities is problematic, the data would have to be serialized (parcels), which induces object mutation problems...
 */

public class IntentSimpleton {
	private Map<String, Object> _vals = new HashMap<>();
	
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
