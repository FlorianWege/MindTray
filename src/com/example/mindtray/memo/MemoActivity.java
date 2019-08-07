package com.example.mindtray.memo;

import java.util.ArrayList;
import java.util.List;

import com.example.mindtray.R;
import com.example.mindtray.memo.Memo.ContentAddedListener;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Point;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.Window;
import android.view.ViewGroup.LayoutParams;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

public class MemoActivity extends FragmentActivity {
	private TextView _textView_title;
	private HorizontalScrollView _scrollView_tabHost;
	private TabHost _tabHost;
	private ViewPager _pager;
	private MemoContentPagerAdapter _pagerAdapter;
	private Memo _memo;
	
	private boolean _isChangingModel = false;
	private int _pagerNextIndex = 0;
	
	private List<TabSpec> _tabs = new ArrayList<TabSpec>();
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		
		inflater.inflate(R.menu.memo, menu);

		return true;
	}
	
	private void updatePages() {
		for (int i = 0; i < _tabHost.getTabWidget().getChildCount(); i++) {
			View tabView = _tabHost.getTabWidget().getChildAt(i);
			
			Drawable drawable = (tabView == _tabHost.getCurrentTabView()) ? getResources().getDrawable(R.drawable.activity_memo_tab_active) : getResources().getDrawable(R.drawable.activity_memo_tab_inactive);
			
			tabView.setBackgroundDrawable(drawable);
		}
	}
	
	private void setPage(int index) {		
		if (_tabHost.getCurrentTabView() != null) {
			updatePages();
		}
		
		if (_tabHost.getCurrentTab() != index) {
			_tabHost.setCurrentTab(index);
		}
		if (_pager.getCurrentItem() != index) {
			_pager.setCurrentItem(index, true);
		}
		
		_scrollView_tabHost = (HorizontalScrollView) findViewById(R.id.scrollView_tabHost);
		
		//not smooth yet
		new Handler().post(new Runnable() {
			@Override
			public void run() {
				_scrollView_tabHost.setSmoothScrollingEnabled(true);
				_scrollView_tabHost.smoothScrollTo((int) (_tabHost.getCurrentTabView().getX() - (_scrollView_tabHost.getWidth() - _tabHost.getCurrentTabView().getWidth()) / 2), 0);
			}
		});

		updatePages();
	}
	
	private final double TAB_WIDTH_REL = 0.25D;
	private final int TAB_HEIGHT = 50;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_memo);
		
		Intent intent = getIntent();
		
		_memo = (Memo) IntentSimpleton.getInstance().get("memo");//(Memo) intent.getExtras().getSerializable("memo");
		
		Log.e(getClass().getSimpleName(), _memo.toString());
		
		setTitle(_memo.toString());
		
		_textView_title = (TextView) findViewById(R.id.textView_title);
		
		_textView_title.setText(_memo.toString());
		//_textView_title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/AlexBrush-Regular-OTF.otf"));
		
		_tabHost = (TabHost) findViewById(R.id.tabHost);
		
		_tabHost.setup();
		
		_tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				int index = _tabHost.getCurrentTab();
				
				_pagerNextIndex = index;
				
				if (_isChangingModel) return;
				
				setPage(index);
			}
		});
		
		_pager = (ViewPager) findViewById(R.id.pager);
		
		_memo.addContentAddedListener(new ContentAddedListener() {
			@Override
			public void handle(MemoContent content, boolean added, int pos) {
				_isChangingModel = true;
				
				if (added) {
					TabSpec newTab = _tabHost.newTabSpec("");
					
					String text = content.toString();
					
					if (text == null) text = Integer.valueOf(pos).toString();
					
					String typeText = null;
					
					if (content instanceof TextContent) {
						typeText = "[text]";
					} else if (content instanceof ImageContent) {
						typeText = "[image]";
					} else if (content instanceof AudioContent) {
						typeText = "[audio]";
					}
					
					LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					
					View indicator = inflater.inflate(R.layout.activity_memo_tab, null);
					
					Point size = new Point();
					
					getWindowManager().getDefaultDisplay().getSize(size);
					
					indicator.setMinimumWidth((int) (TAB_WIDTH_REL * size.x));
					indicator.setMinimumHeight(TAB_HEIGHT);
					
					TextView textView_name = (TextView) indicator.findViewById(R.id.textView_name);
					TextView textView_type = (TextView) indicator.findViewById(R.id.textView_type);
					
					textView_name.setText(text);
					textView_type.setText(typeText);
					
					Log.e("type", typeText);
					
					newTab.setContent(new TabContentFactory() {
						@Override
						public View createTabContent(String tag) {
							return new LinearLayout(MemoActivity.this);
						}
					});
					newTab.setIndicator(indicator);

					_tabHost.setup();
					
					_tabHost.clearAllTabs();
					
					if (_tabs.size() > 0) {
						_tabs.add(_tabs.size() - 1, newTab);
					}
					
					for (TabSpec tab : _tabs) {
						_tabHost.addTab(tab);
					}
					
					setPage(_tabHost.getTabWidget().getChildCount() - 2);
				} else {
					_tabHost.getTabWidget().removeView(_tabHost.getTabWidget().getChildTabViewAt(pos));
				}
				
				_isChangingModel = false;
			}
		});
		
		{
			TabSpec tab = _tabHost.newTabSpec("create");
			
			LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			LinearLayout indicator = (LinearLayout) inflater.inflate(R.layout.activity_memo_tab_create, null);
			
			Point size = new Point();
			
			getWindowManager().getDefaultDisplay().getSize(size);
			
			indicator.setMinimumWidth((int) (TAB_WIDTH_REL * size.x));
			indicator.setMinimumHeight(TAB_HEIGHT);
			
			TextView textView_name = (TextView) indicator.findViewById(R.id.textView_name);
	
			textView_name.setText("<create>");
			
			tab.setIndicator(indicator);
			tab.setContent(new TabContentFactory() {
				@Override
				public View createTabContent(String tag) {
					return new LinearLayout(MemoActivity.this);
				}
			});
	
			_tabs.add(tab);		
			_tabHost.addTab(tab);
		}
		
		//prevents tabHost from stealing focus
		_tabHost.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
			
			@Override
			public void onViewDetachedFromWindow(View v) {
			}
			
			@Override
			public void onViewAttachedToWindow(View v) {
				_tabHost.getViewTreeObserver().removeOnTouchModeChangeListener(_tabHost);
			}
		});

		_tabHost.setCurrentTab(0);
		
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
				setPage(pos);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}
}
