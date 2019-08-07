package com.example.mindtray;

import com.example.mindtray.memo.MemoListActivity;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import build.BuildInfo;

public class MainActivity extends Activity {
	private TextView _textView_buildNo;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		Typeface titleFont = Typeface.createFromAsset(getAssets(), "fonts/AlexBrush-Regular-OTF.otf");
		
		((TextView) findViewById(R.id.textView_title)).setTypeface(titleFont);
		
		_textView_buildNo = (TextView) findViewById(R.id.textView_buildNo);
		
		_textView_buildNo.setText(String.format("build: %s", BuildInfo.BUILD));
	}
	
	public void button_calendar_onClick(View view) {
		Intent intent = new Intent(this, CalendarActivity.class);
		
		startActivity(intent);
	}
	
	public void button_memos_onClick(View view) {		
		Intent intent = new Intent(this, MemoListActivity.class);
		
		startActivity(intent);
	}
	
	public void button_exit_onClick(View view) {
		android.os.Process.killProcess(android.os.Process.myPid());
		System.exit(0);
	}
}
