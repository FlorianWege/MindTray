package com.example.mindtray.memo;

import com.example.mindtray.R;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MemoContentFragment extends Fragment {
	private MemoContent _memoContent;
	
	private LinearLayout _layout_type;
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.memo_content, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.option_delete: {
			_memoContent.remove();

			break;
		}
		}
		
		return true;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_memo_content, container, false);
		
		_layout_type = (LinearLayout) view.findViewById(R.id.layout_type);
		
		if (_memoContent instanceof TextContent) {
			LayoutInflater typeInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			View typeRoot = typeInflater.inflate(R.layout.fragment_text_content, _layout_type);
			
			EditText editText = (EditText) typeRoot.findViewById(R.id.editText);
			
			editText.setText(((TextContent) _memoContent).getText());
		}
		
		TextView testView = (TextView) view.findViewById(R.id.textView_memoContent);
		
		Log.d("set testView", _memoContent.toString() == null ? "empty" : _memoContent.toString());
		testView.setText(_memoContent.toString());
		
		//testView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		setHasOptionsMenu(true);
		
		return view;
	}
	
	public MemoContentFragment(MemoContent memoContent) {
		_memoContent = memoContent;
	}
}
