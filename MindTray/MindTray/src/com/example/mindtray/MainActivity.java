package com.example.mindtray;

import com.example.mindtray.memo.MemoListActivity;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import build.BuildInfo;

public class MainActivity extends Activity {
	private TextView _buildNoTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		_buildNoTextView = (TextView) findViewById(R.id.buildNoTextView);
		
		_buildNoTextView.setText(String.format("build: %s", BuildInfo.BUILD));
	}
	
	public void calendarBtn_onClick(View view) {
		Intent intent = new Intent(this, CalendarActivity.class);
		
		startActivity(intent);
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
