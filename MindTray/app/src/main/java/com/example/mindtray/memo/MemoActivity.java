package com.example.mindtray.memo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import com.example.mindtray.shared.ConfirmDialog;
import com.example.mindtray.shared.IntentSimpleton;
import com.example.mindtray.R;
import com.example.mindtray.shared.MyFragmentActivity;
import com.example.mindtray.shared.Storage;
import com.example.mindtray.shared.Util;
import com.example.mindtray.memo.Memo.Listener;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class MemoActivity extends MyFragmentActivity {
	private Button _button_memoList;

	private TextView _textView_title;
	private HorizontalScrollView _scrollView_tabHost;
	private TabHost _tabHost;
	private ViewPager _pager;
	private ContentPagerAdapter _pagerAdapter;
	private Memo _memo;
	
	private boolean _isChangingModel = false;
	private int _pagerNextIndex = 0;
	
	private List<TabSpec> _tabs = new ArrayList<>();
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		
		inflater.inflate(R.menu.memo, menu);

		return true;
	}
	
	private void updatePages() {
		for (int i = 0; i < _tabHost.getTabWidget().getChildCount(); i++) {
			View tabView = _tabHost.getTabWidget().getChildAt(i);
			
			Drawable drawable = (tabView == _tabHost.getCurrentTabView()) ? getResources().getDrawable(R.drawable.tab_active) : getResources().getDrawable(R.drawable.tab_inactive);
			
			tabView.findViewById(R.id.button).setBackgroundDrawable(drawable);
		}
	}
	
	private void setPage(int index) {
		if (_tabHost.getCurrentTabView() != null) {
			updatePages();
		}
		
		if (_tabHost.getCurrentTab() != index) {
			int tabIndex = Math.min(_tabs.size() - 1, index);

			_tabHost.setCurrentTab(tabIndex);
		}
		if (_pager.getCurrentItem() != index) {
			int pagerIndex = Math.min(_tabs.size() - 1, index);

			_pager.setCurrentItem(pagerIndex, true);
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

	private void updateTitle() {
		String title = _memo.toString();

		super.setTitle(title);

		Calendar date = _memo.getDate();

		if (date != null) {
			title = title + String.format(" - %s", Util.formatDate(date));
		}

		_textView_title.setText(title);
	}

	private void recreateTabs() {
		_tabHost.setup();

		_tabHost.clearAllTabs();

		for (TabSpec tab : _tabs) {
			_tabHost.addTab(tab);
		}

		updatePages();

		_pager.setAdapter(null);
		_pager.setAdapter(_pagerAdapter);
	}

	private void addTab(int pos, final Content content) {
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

		indicator.setMinimumWidth((int) (TAB_WIDTH_REL * Util.getScreenDimensions(getActivity()).x));
		indicator.setMinimumHeight(TAB_HEIGHT);

		TextView textView_name = (TextView) indicator.findViewById(R.id.textView_name);
		TextView textView_type = (TextView) indicator.findViewById(R.id.textView_type);
		Button button = (Button) indicator.findViewById(R.id.button);

		textView_name.setText(text);
		textView_type.setText(typeText);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_tabHost.setCurrentTab(_memo.getContents().indexOf(content));
			}
		});
		button.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				ConfirmDialog dialog = new ConfirmDialog();

				dialog.setArgs(String.format("Remove content '%s' from this memo?", content.getName()), new ConfirmDialog.Listener() {
					@Override
					public void onDecline() {

					}

					@Override
					public void onAccept() {
						try {
							content.remove();
						} catch (Storage.StorageException e) {
							Util.printException(getActivity(), e);
						}
					}

					@Override
					public void onDismiss() {

					}
				});

				showDialog(dialog);

				return true;
			}
		});

		newTab.setContent(new TabContentFactory() {
			@Override
			public View createTabContent(String tag) {
				return new LinearLayout(MemoActivity.this);
			}
		});
		newTab.setIndicator(indicator);

		if (_tabs.size() > 0) {
			_tabs.add(_tabs.size() - 1, newTab);
		}

		recreateTabs();

		setPage(_tabHost.getTabWidget().getChildCount() - 2);
	}

	private void addCreateTab() {
		TabSpec tab = _tabHost.newTabSpec("create");

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		LinearLayout indicator = (LinearLayout) inflater.inflate(R.layout.activity_memo_tab_create, null);

		indicator.setMinimumWidth((int) (TAB_WIDTH_REL * Util.getScreenDimensions(getActivity()).x));
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

		Button button = (Button) indicator.findViewById(R.id.button);

		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				_tabHost.setCurrentTabByTag("create");
			}
		});

		_tabs.add(tab);
		_tabHost.addTab(tab);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_memo);
		
		_memo = (Memo) IntentSimpleton.getInstance().get("memo");//(Memo) intent.getExtras().getSerializable("memo");

		_button_memoList = (Button) findViewById(R.id.button_memoList);

		_textView_title = (TextView) findViewById(R.id.textView_title);

		updateTitle();

		//_textView_title.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/AlexBrush-Regular-OTF.otf"));
		
		_tabHost = (TabHost) findViewById(R.id.tabHost);
		
		_tabHost.setup();
		
		_tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				int index = _tabHost.getCurrentTab();
				
				_pagerNextIndex = index;
				
				if (_isChangingModel) {
					updatePages();

					return;
				}
				
				setPage(index);
			}
		});
		
		_pager = (ViewPager) findViewById(R.id.pager);
		
		_memo.addListener(new Listener() {
			@Override
			public void contentAdded(Content content, boolean added, int pos) {
				_isChangingModel = true;

				if (added) {
					addTab(pos, content);
				} else {
					_tabs.remove(pos);
					recreateTabs();
				}
				
				_isChangingModel = false;
			}

			@Override
			public void contentChanged() {

			}
		});

		new Handler().post(new Runnable() {
			@Override
			public void run() {
				List<Content> contents = new ArrayList<>(_memo.getContents());

				for (Content content : contents) {
					addTab(contents.indexOf(content), content);
				}
			}
		});

		addCreateTab();
		
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
		
		_pagerAdapter = new ContentPagerAdapter(getSupportFragmentManager(), _memo);
		
		_pager.setAdapter(_pagerAdapter);
		
		/*_pagerAdapter.addOnDataSetChangedListener(new ContentPagerAdapter.OnDataSetChangedListener() {
			@Override
			public void handle() {
				_pager.setCurrentItem(_pagerNextIndex);
			}
		});*/
		
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

		Util.animateBackground(this, (ImageView) findViewById(R.id.bg1));
	}

	public void button_memoList_onClick(View sender) {
		finish();
	}
}