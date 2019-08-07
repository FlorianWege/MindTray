package com.example.mindtray;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.Toast;

public class MemoListActivity extends Activity {	
	ListView _memosListView;
	MemoAdapter _adapter;
	
	private void startMemo() {
		Intent intent = new Intent(this, MemoActivity.class);
		
		startActivity(intent);
	}
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_memolist);
		
		_memosListView = (ListView) findViewById(R.id.listView_memos);
		
		_adapter = new MemoAdapter(getApplicationContext(), null);
		
		_memosListView.setAdapter(_adapter);
		
		_memosListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				startMemo();
			}

		});
	}
	
	public void backBtn_onClick(View view) {
		finish();
	}
	
	public void newMemoBtn_onClick(View view) {
		NewMemoDialogFragment dialog = new NewMemoDialogFragment();
    	
    	FragmentManager fm = getFragmentManager();
    	
    	dialog.show(fm, "Memos");
	}

	public void onFinishSetText(String name) {
		if (name == null) {
			Toast.makeText(getApplicationContext(), "Es wurde kein Name angegeben.", Toast.LENGTH_LONG).show();
			
			return;
		}
		
		//_adapter.add(name);
		Memo memo = new Memo(name);
		
		_adapter.add(memo);
		
		_adapter.notifyDataSetChanged();
	}
}
