package com.example.mindtray;

import android.app.ActionBar.Tab;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class MemoActivity extends Activity {
	Memo _memo;
	
	TabHost _tabHost;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_memo);
		
		
		Memo memo = new Memo("abc");
		
		MemoTextContent con = new MemoTextContent("def");
		
		con.setText("zzzzzzzzzzzzzzzzzz");
		
		memo.addContent(con);
		
		_memo = memo;
		
		
		_tabHost = (TabHost) findViewById(R.id.tabhost);
		
		_tabHost.setup();

		TabSpec createTab = _tabHost.newTabSpec("create");

		createTab.setIndicator("create"); 
		
		createTab.setContent(new TabContentFactory() {
			@Override
			public View createTabContent(String tag) {
				Button button = new Button(getApplicationContext());
				
				button.setText("click here");
				
				button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						_tabHost.clearAllTabs();
					}
				});
				
				return button;
			}
		});

		_tabHost.addTab(createTab); 
		
		for (MemoContent content : memo.getContents()) {
			System.err.println("add " + content.toString());
			TabSpec tab = _tabHost.newTabSpec(content.toString());
			
			tab.setIndicator(content.toString());
			
			tab.setContent(new TabContentFactory() {
				@Override
				public View createTabContent(String contentName) {
					MemoContent content = _memo.getContent(contentName);
					
					LinearLayout layout = new LinearLayout(getApplicationContext());
					
					if (content instanceof MemoTextContent) {
						TextView textView = new TextView(getApplicationContext());
						
						textView.setText(((MemoTextContent) content).getText());
						
						layout.addView(textView);
					}
					
					return layout;
				}
			});
			
			_tabHost.addTab(tab);
		}
		
		_tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				System.out.println(tabId);
			}
		});
		
		_tabHost.setBackgroundColor(Color.RED);
	}
}
