package com.example.mindtray.memo;

import com.example.mindtray.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MemoContentFragment extends Fragment {
	private MemoContent _memoContent;
	
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
		
		TextView testView = (TextView) view.findViewById(R.id.memoContentTextView);
		
		testView.setText(_memoContent.toString());
		
		setHasOptionsMenu(true);
		
		return view;
	}
	
	public MemoContentFragment(MemoContent memoContent) {
		_memoContent = memoContent;
	}
}
