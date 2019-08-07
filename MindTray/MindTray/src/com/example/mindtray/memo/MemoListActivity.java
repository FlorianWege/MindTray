package com.example.mindtray.memo;

import java.util.Date;

import com.example.mindtray.R;
import com.example.mindtray.R.id;
import com.example.mindtray.R.layout;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

public class MemoListActivity extends Activity {	
	private ListView _memosListView;
	private MemoAdapter _adapter;
	
	private void startMemo(Memo memo) {
		Intent intent = new Intent(this, MemoActivity.class);
		
		intent.putExtra("memo", memo);
		
		startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_memolist);
		
		Date date = (Date) getIntent().getSerializableExtra("date");
		
		if (date != null) {
			setTitle(String.format("Memos (%s)", DateFormat.format("yyyy-MM-dd)", date)));
		}
		
		_memosListView = (ListView) findViewById(R.id.listView_memos);
		
		_adapter = new MemoAdapter(getApplicationContext(), null);
		
		_memosListView.setAdapter(_adapter);
		
		_memosListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				startMemo(_adapter.getItem(pos));
			}
		});
		
		Log.e("listactivity", "created");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.memo_list, menu);
		
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.option_search).getActionView();
		Log.e("menu", menu.findItem(R.id.option_search) + ";" + menu.findItem(R.id.option_search).getActionView());
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		
		return true;
	}
	
	public void backBtn_onClick(View view) {
		Log.e("listactivity", "finish");
		finish();
	}
	
	public void newMemoBtn_onClick(View view) {
		NewMemoDialogFragment dialog = new NewMemoDialogFragment();
    	
    	FragmentManager fm = getFragmentManager();
    	
    	dialog.show(fm, "Memos");
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		Log.e("memolist", "searchintent");
		
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			
		}
	}

	public void onFinishSetText(String name) {
		if ((name == null) || name.isEmpty()) {
			Toast.makeText(getApplicationContext(), "Es wurde kein Name angegeben.", Toast.LENGTH_LONG).show();
			
			return;
		}
		
		//_adapter.add(name);
		Memo memo = new Memo(name);
		
		_adapter.add(memo);
		
		_adapter.notifyDataSetChanged();
		
		startMemo(memo);
	}
}
