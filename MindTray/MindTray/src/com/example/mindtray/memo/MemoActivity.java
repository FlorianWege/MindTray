package com.example.mindtray.memo;

import com.example.mindtray.R;
import com.example.mindtray.memo.Memo.ContentAddedListener;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;

public class MemoActivity extends FragmentActivity implements ActionBar.TabListener {
	private ActionBar _actionBar;
	private ViewPager _pager;
	private MemoContentPagerAdapter _pagerAdapter;
	private Memo _memo;
	
	private boolean _isChangingModel = false;
	private int _pagerNextIndex = 0;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		
		inflater.inflate(R.menu.memo, menu);

		return true;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("memoactvity", "create");
		setContentView(R.layout.activity_memo);
		
		_memo = (Memo) getIntent().getExtras().getSerializable("memo");
		
		setTitle(_memo.toString());
		
		_actionBar = getActionBar();
		
		_actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		_pager = (ViewPager) findViewById(R.id.pager);
		
		_memo.addContentAddedListener(new ContentAddedListener() {
			@Override
			public void handle(MemoContent content, boolean added, int pos) {
				_isChangingModel = true;
				
				if (added) {
					Tab tab = _actionBar.newTab();
					
					String text = content.toString();
					
					if (text == null) text = new Integer(pos).toString();
					
					if (content instanceof TextContent) {
						text += " [text]";
						//tab.setIcon(resId);
					} else if (content instanceof ImageContent) {
						text += " [image]";
						//tab.setIcon(resId);
					} else if (content instanceof AudioContent) {
						text += " [audio]";
						
						//tab.setIcon(resId);
					}
					
					tab.setText(text);
					tab.setTabListener(MemoActivity.this);
					
					_actionBar.addTab(tab, pos);
					
					_actionBar.selectTab(tab);
				} else {
					_actionBar.removeTabAt(pos);
				}
				
				_isChangingModel = false;
			}
		});
		
		_actionBar.addTab(_actionBar.newTab().setText("<create>").setTabListener(MemoActivity.this));
		
		for (int i = 0; i < 5; i++) {
			_memo.addContent(new TextContent(null));
		}
		
		_actionBar.selectTab(_actionBar.getTabAt(0));
		
		_pagerAdapter = new MemoContentPagerAdapter(getSupportFragmentManager(), _memo);
		
		_pager.setAdapter(_pagerAdapter);
		
		_pagerAdapter.addOnDataSetChangedListener(new MemoContentPagerAdapter.OnDataSetChangedListener() {
			@Override
			public void handle() {
				_pager.setCurrentItem(_pagerNextIndex);
			}
		});
		
		_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageSelected(int pos) {
				_actionBar.setSelectedNavigationItem(pos);
				_pager.setCurrentItem(pos);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
		
		/*_memo.addContent(new MemoTextContent("a"));
		_memo.addContent(new MemoTextContent("b"));
		_memo.addContent(new MemoTextContent("c"));
		_memo.addContent(new MemoTextContent("d"));
		_memo.addContent(new MemoTextContent("e"));*/
		
		/*_tabHost = (TabHost) findViewById(R.id.tabhost);
		
		_tabHost.setup();

		TabSpec createTab = _tabHost.newTabSpec("<create>");

		createTab.setIndicator("<create>"); 
		
		createTab.setContent(new TabContentFactory() {
			@Override
			public View createTabContent(String tag) {
				Button button = new Button(getApplicationContext());
				
				button.setText("click here");
				
				button.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						System.err.println("click create");
						
						//_tabHost.clearAllTabs();
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
		
		_tabHost.setBackgroundColor(Color.RED);*/
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		_pagerNextIndex = tab.getPosition();
		
		if (_isChangingModel) return;
		
		_pager.setCurrentItem(tab.getPosition(), true);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		_pager.setCurrentItem(tab.getPosition(), true);
		
	}
}
