package com.example.mindtray.memo;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import com.example.mindtray.R;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

public class MemoListActivity extends Activity {
	private TextView _textView_title;
	
	private ListView _listView;
	private MemoAdapter _adapter;
	
	private void startMemo(Memo memo) {
		IntentSimpleton.getInstance().set("memo", memo);
		
		Intent intent = new Intent(this, MemoActivity.class);
		
		//intent.putExtra("memo", memo);
		
		startActivity(intent);
	}
	
	@Override
	public void setTitle(CharSequence text) {
		super.setTitle(text);
		
		_textView_title.setText(text);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_memolist);
		
		_textView_title = (TextView) findViewById(R.id.textView_title);
		
		Typeface titleFont = Typeface.createFromAsset(getAssets(), "fonts/AlexBrush-Regular-OTF.otf");
		
		_textView_title.setTypeface(titleFont);
		
		Intent intent = getIntent();
		
		if (intent != null) {
			Bundle extras = intent.getExtras();
			
			if (extras != null) {
				Date date = (Date) extras.getSerializable("date");
				
				if (date != null) {
					Log.e("listactivity", "intent " + date);
					setTitle(String.format("Memos (%s)", DateFormat.format("yyyy-MM-dd", date)));
				}
			}
		}
		
		_listView = (ListView) findViewById(R.id.listView_memos);
		
		_adapter = new MemoAdapter(getApplicationContext(), null);
		
		_listView.setAdapter(_adapter);
		
		_listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				startMemo(_adapter.getItem(pos));
			}
		});
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
	
	public void button_back_onClick(View view) {
		finish();
	}
	
	private boolean _startMemoFlag = false;
	
	public void button_create_onClick(View view) {
		NewMemoDialogFragment dialog = new NewMemoDialogFragment();
    	
    	FragmentManager fm = getFragmentManager();
    	
    	dialog.setStartMemoFlag(_startMemoFlag);
    	
    	dialog.show(fm, "Memos");
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		
		Log.e("memolist", "searchintent");
		
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			
		}
	}
	
	private void clear() {
		_adapter.clear();
		
		_adapter.notifyDataSetChanged();
	}
	
	private void addMemo(Memo memo) {
		Log.e(getClass().getSimpleName(), "_" + memo + "_" + System.identityHashCode(memo.getContents()));
		
		_adapter.add(memo);
		
		_adapter.notifyDataSetChanged();
	}

	public void onFinishSetText(String name, boolean startMemo) {
		if ((name == null) || name.isEmpty()) {
			Toast.makeText(getApplicationContext(), getString(R.string.memolist_error_no_name), Toast.LENGTH_LONG).show();
			
			return;
		}
		
		//_adapter.add(name);
		Memo memo = new Memo(name);
		
		addMemo(memo);
		
		_startMemoFlag = startMemo;
		
		if (startMemo) startMemo(memo);
	}
	
	public void load_onClick(View view) {
		clear();

		File inFile = new File(Environment.getExternalStorageDirectory(), "abc.memo");

		try {
			FileInputStream in = new FileInputStream(inFile);
			
			ObjectInputStream objIn = new ObjectInputStream(in);
			
			Object obj;
			
			while ((obj = objIn.readObject()) != null) {
				Memo memo = (Memo) obj;
				
				Log.e(getClass().getSimpleName(), "read " + memo + " with " + memo.getContents().size());
				
				addMemo(memo);
			}
			
			objIn.close();
			
			in.close();
		} catch (EOFException e) {
			Log.e(getClass().getSimpleName(), "eof");
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), e.getMessage(), e);
		}
	}
	
	public void save_onClick(View view) {
		File outFile = new File(Environment.getExternalStorageDirectory(), "abc.memo");

		try {
			FileOutputStream out = new FileOutputStream(outFile);
			
			ObjectOutputStream objOut = new ObjectOutputStream(out);
			
			for (int i = 0; i < _adapter.getCount(); i++) {
				Memo memo = _adapter.getItem(i);
				
				Log.e(getClass().getSimpleName(), "write " + memo + " with " + System.identityHashCode(memo.getContents()) + ";" + memo.getContents().size());
				objOut.writeObject(_adapter.getItem(i));
			}
			
			objOut.close();
			
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e(getClass().getSimpleName(), e.getMessage());
		}
	}
}
