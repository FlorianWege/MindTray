package com.example.mindtray;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	public void calendarBtn_onClick(View view) {
		Log.d("abc", "calendar");
	}
	
	public void memosBtn_onClick(View view) {		
		Intent intent = new Intent(this, MemoListActivity.class);
		
		startActivity(intent);
	}
	
	public void exitBtn_onClick(View view) {
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}
}
