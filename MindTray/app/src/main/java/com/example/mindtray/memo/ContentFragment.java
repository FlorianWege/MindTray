package com.example.mindtray.memo;

import com.example.mindtray.R;
import com.example.mindtray.shared.Storage;
import com.example.mindtray.shared.Util;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/*
rather obsolete at the moment, everything handled by the sub classes
 */

public class ContentFragment extends Fragment {
	private Content _memoContent;
	
	private LinearLayout _layout_type;
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.memo_content, menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.option_delete: {
			try {
				_memoContent.remove();
			} catch (Storage.StorageException e) {
				Util.printException(getActivity(), e);
			}

			break;
		}
		}
		
		return true;
	}

	public void setInnerFragment(ContentFragment frag) {
		FragmentTransaction ft = getFragmentManager().beginTransaction();

		ft.add(R.id.layout_type, frag);

		ft.commit();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_memo_content, container, false);
		
		_layout_type = (LinearLayout) view.findViewById(R.id.layout_type);
		
		TextView testView = (TextView) view.findViewById(R.id.textView_memoContent);

		testView.setText(_memoContent.getName());
		testView.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/AlexBrush-Regular-OTF.otf"));
		
		//testView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		
		setHasOptionsMenu(true);
		
		return view;
	}

	public void setArgs(Content memoContent) {
		_memoContent = memoContent;
	}

	public ContentFragment() {

	}
}
