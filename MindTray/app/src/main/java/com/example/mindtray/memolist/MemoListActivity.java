package com.example.mindtray.memolist;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import com.example.mindtray.CalendarActivity;
import com.example.mindtray.MainActivity;
import com.example.mindtray.R;
import com.example.mindtray.shared.ConfirmDialog;
import com.example.mindtray.shared.IntentSimpleton;
import com.example.mindtray.shared.MyActivity;
import com.example.mindtray.shared.Storage;
import com.example.mindtray.shared.Util;
import com.example.mindtray.memo.Memo;
import com.example.mindtray.memo.MemoActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/*
	lists up the created memo objects
 */

public class MemoListActivity extends MyActivity {
	private Button _button_main;
	private Button _button_calendar;

	private TextView _textView_title;
	
	private ListView _listView;
	private MemoListAdapter _adapter;

	private ViewPager _pager;

	private Button _button_prevDate;
	private Button _button_nextDate;

	private Calendar _date = null;
	private boolean _showAll = false;
	
	private void startMemo(Memo memo) {
		//enter memo
		IntentSimpleton.getInstance().set("memo", memo);
		
		Intent intent = new Intent(this, MemoActivity.class);
		
		//intent.putExtra("memo", memo);

		startActivityForResult(intent, 0);
	}
	
	@Override
	public void setTitle(CharSequence text) {
		//set title at the top
		super.setTitle(text);
		
		_textView_title.setText(text);
	}

	public void updateTitle() {
		//depending on the mode, update title
		if (_showAll) {
			setTitle(String.format(getString(R.string.memolist_title), Integer.toString(_adapter.getCount())));
		} else {
			if (_date == null) {
				setTitle("");
			} else {
				setTitle(String.format(getString(R.string.memolist_title_with_date), Util.formatDate(_date), Integer.toString(_adapter.getCount())));
			}
		}
	}

	public void clearList() {
		_adapter.clear();

		_adapter.notifyDataSetChanged();

		updateTitle();
	}

	private void updateShownMemos() {
		//filter the memos to display
		clearList();

		List<Memo> storageMemos = Storage.getInstance(this).getMemos();
		List<Memo> adapterItems = _adapter.getItems();

		for (Memo memo : storageMemos) {
			Calendar memoDate = memo.getDate();

			if (!_showAll) {
				if (_date == null) {
					break;
				} else {
					if (_date.get(Calendar.YEAR) != memoDate.get(Calendar.YEAR)) continue;
					if (_date.get(Calendar.MONTH) != memoDate.get(Calendar.MONTH)) continue;
					if (_date.get(Calendar.DAY_OF_MONTH) != memoDate.get(Calendar.DAY_OF_MONTH)) continue;
				}
			}

			if (!adapterItems.contains(memo)) {
				adapterItems.add(memo);
			}

			_adapter.notifyDataSetChanged();
		}

		updateTitle();
	}

	private void setDate(Calendar date) {
		//set displayed date, update control captions
		date = (date == null) ? new GregorianCalendar() : date;

		_date = date;

		updateShownMemos();

		Calendar prevDate = new GregorianCalendar();

		prevDate.setTime(_date.getTime());

		prevDate.add(Calendar.DAY_OF_MONTH, -1);

		_button_prevDate.setText(String.format("< %s <", Util.formatDate(prevDate)));

		Calendar nextDate = new GregorianCalendar();

		nextDate.setTime(_date.getTime());

		nextDate.add(Calendar.DAY_OF_MONTH, 1);

		_button_nextDate.setText(String.format("> %s >", Util.formatDate(nextDate)));

		int pos = MemoListPagerAdapter.dateToPos(_date);

		if (_pager.getCurrentItem() != pos) {
			_pager.setCurrentItem(pos);
		}
	}

	private void showAll(boolean val) {
		_showAll = val;

		updateShownMemos();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_memolist);

		_button_main = (Button) findViewById(R.id.button_main);
		_button_calendar = (Button) findViewById(R.id.button_calendar);

		_button_prevDate = (Button) findViewById(R.id.button_prevDate);
		_button_nextDate = (Button) findViewById(R.id.button_nextDate);
		
		_textView_title = (TextView) findViewById(R.id.textView_title);
		
		Typeface titleFont = Typeface.createFromAsset(getAssets(), "fonts/AlexBrush-Regular-OTF.otf");
		
		//_textView_title.setTypeface(titleFont);
		
		Intent intent = getIntent();
		
		if (intent != null) {
			Bundle extras = intent.getExtras();
			
			if (extras != null) {
				_date = (Calendar) extras.getSerializable("date");
			}
		}

		if (_date != null) {
			Log.e("listactivity", "intent " + _date);
		}

		_listView = (ListView) findViewById(R.id.listView_memos);
		
		_adapter = new MemoListAdapter(getApplicationContext());
		
		_listView.setAdapter(_adapter);
		_listView.setDividerHeight(0);
		
		_listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				Memo memo = _adapter.getItem(pos);

				startMemo(memo);
			}
		});

		registerForContextMenu(_listView);

		/*if (false) {
			_listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {
					View anchorView = new View(getApplicationContext());

					MenuInflater inflater = new MenuInflater(getApplicationContext());

					inflater.inflate(R.menu.memo_list_item, new Menu());

					PopupWindow menu = new PopupWindow();

					ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1);

					adapter.add("Edit");
					adapter.add("Remove");

					menu.setAdapter(adapter);

					menu.setContentView(getSystemService());

				menu.getMenuInflater().inflate(R.menu.memo_list_item, menu.getMenu());

				menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem menuItem) {


						return true;
					}
				});

					menu.setAnchorView(view);
					menu.

							menu.show();

					return true;
				}
			});
		}*/

		_pager = (ViewPager) findViewById(R.id.pager);

		_pager.setAdapter(new MemoListPagerAdapter(new MemoListPagerAdapter.Listener() {
			@Override
			public void onInstantiateItem(ViewGroup container, Calendar date, int pos) {
				View view = new MemoListPage(getApplicationContext(), date);

				container.addView(view);
			}

			@Override
			public void onDestroyItem(ViewGroup container, Calendar date, int pos) {
				//container.removeView();
			}
		}));

		_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				setDate(MemoListPagerAdapter.posToDate(position));
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});

		showAll(_date == null);
		setDate(_date);

		//getSupportActionBar().setDisplayOptions(0);
		//setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
		//getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

		//getSupportActionBar().setCustomView(R.layout.memo_list_actionbar);

		Util.animateBackground(this, (ImageView) findViewById(R.id.bg1));
	}

	public void button_main_onClick(View sender) {
		Intent intent = new Intent(this, MainActivity.class);

		intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);

		startActivity(intent);
	}

	public void button_calendar_onClick(View sender) {
		Intent intent = new Intent(this, CalendarActivity.class);

		intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);

		startActivity(intent);
	}

	public void button_overflow_onClick(View sender) {
		openOptionsMenu();
	}

	private void startEditMemo(Memo memo) {
		EditMemoDialog dialog = new EditMemoDialog();

		dialog.setMemo(memo);

		dialog.setListener(new EditMemoDialog.Listener() {
			@Override
			public void onDismiss() {
				_adapter.notifyDataSetChanged();
			}
		});

		showDialog(dialog);
	}

	private Memo _menu_memo;
	private MenuItem _menu_editItem;
	private MenuItem _menu_removeItem;

	//context menus are nice
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		if (v.getId() == _listView.getId()) {
			AdapterView.AdapterContextMenuInfo contextInfo = (AdapterView.AdapterContextMenuInfo) menuInfo;

			_menu_memo = _adapter.getItem(contextInfo.position);

			menu.setHeaderTitle(_menu_memo.getName());
			_menu_editItem = menu.add("Edit");
			_menu_removeItem = menu.add("Remove");
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		if (item == _menu_editItem) {
			startEditMemo(_menu_memo);
		}

		if (item == _menu_removeItem) {
			try {
				removeMemo(_menu_memo);
			} catch (Storage.StorageException e) {
				Util.printException(getActivity(), e);
			}
		}

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.memo_list, menu);

		MenuItem clearItem = menu.findItem(R.id.option_clear);

		Log.e(getClass().getSimpleName(), _adapter.getCount() + "");

		/*SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.option_search).getActionView();
		Log.e("menu", menu.findItem(R.id.option_search) + ";" + menu.findItem(R.id.option_search).getActionView());
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));*/
		
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		if (!item.isEnabled()) {
			Toast.makeText(getApplicationContext(), "option is disabled", Toast.LENGTH_SHORT).show();

			return false;
		}

		switch (item.getItemId()) {
			case R.id.option_clear: {
				if (_adapter.isEmpty()) {
					Toast.makeText(getApplicationContext(), "nothing to clear", Toast.LENGTH_SHORT).show();

					break;
				}

				ConfirmDialog dialog = new ConfirmDialog();

				dialog.setArgs("Discard all memos?", new ConfirmDialog.Listener() {
					@Override
					public void onDecline() {

					}

					@Override
					public void onAccept() {
						clear();
					}

					@Override
					public void onDismiss() {
					}
				});

				showDialog(dialog);

				break;
			}
		}

		return super.onMenuItemSelected(featureId, item);
	}
	
	private boolean _startMemoFlag = false;
	
	public void button_create_onClick(View view) {
		NewMemoDialog dialog = new NewMemoDialog();
    	
    	dialog.setArgs(_startMemoFlag, new NewMemoDialog.Listener() {
			@Override
			public void onFail(Exception e) {

			}

			@Override
			public void onSuccess(Memo memo, boolean startMemo) {
				try {
					addMemo(memo, startMemo);
				} catch (Storage.StorageException e) {
					Util.printException(getActivity(), e);
				}
			}
		});

		dialog.setListener(new EditMemoDialog.Listener() {
			@Override
			public void onDismiss() {
				_adapter.notifyDataSetChanged();
			}
		});

    	showDialog(dialog);
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		Log.e("memolist", "searchintent");
		
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			
		}
	}
	
	private void clear() {
		clearList();
//TODO: externalize strings
		Toast.makeText(getApplicationContext(), String.format("cleared all items"), Toast.LENGTH_SHORT).show();
	}
	
	private void addMemo(Memo memo) throws Storage.StorageException {
		Storage.getInstance(this).addMemo(memo);

		updateShownMemos();

		Toast.makeText(getApplicationContext(), String.format("added item \"%s\"", memo.getName()), Toast.LENGTH_SHORT).show();
	}

	private void removeMemo(Memo memo) throws Storage.StorageException {
		Storage.getInstance(this).removeMemo(memo);

		updateShownMemos();

		Toast.makeText(getApplicationContext(), String.format("removed item \"%s\"", memo.getName()), Toast.LENGTH_SHORT).show();
	}

	public void addMemo(Memo memo, boolean start) throws Storage.StorageException {
		addMemo(memo);
		
		_startMemoFlag = start;
		
		if (start) startMemo(memo);
	}

	public void button_showAll_onClick(View sender) {
		showAll(!_showAll);
	}

	public void button_prevDate_onClick(View sender) {
		//switch to previous date, update displayed memos
		Calendar date = new GregorianCalendar();

		date.setTime(_date.getTime());

		date.add(Calendar.DAY_OF_MONTH, -1);

		setDate(date);
		showAll(false);
	}

	public void button_nextDate_onClick(View sender) {
		//switch to next date, update displayed memos
		Calendar date = new GregorianCalendar();

		date.setTime(_date.getTime());

		date.add(Calendar.DAY_OF_MONTH, 1);

		setDate(date);
		showAll(false);
	}
}