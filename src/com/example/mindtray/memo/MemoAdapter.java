package com.example.mindtray.memo;

import java.util.ArrayList;
import java.util.List;

import com.example.mindtray.R;
import com.example.mindtray.R.id;
import com.example.mindtray.R.layout;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MemoAdapter extends ArrayAdapter<Memo> {
	private Context _context;
	
	public MemoAdapter(Context context, ArrayList<Memo> objects) {
		super(context, android.R.layout.simple_list_item_2);
		
		_context = context;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {		
		Memo memo = getItem(pos);
		
		if (convertView == null) convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_memos_listview, parent, false);
		
		TextView nameView = (TextView) convertView.findViewById(R.id.textView_name);
		
		Typeface nameFont = Typeface.createFromAsset(_context.getAssets(), "fonts/AlexBrush-Regular-OTF.otf");
		
		nameView.setTypeface(nameFont);
		
		nameView.setText(memo.toString());
		
		return convertView;
	}
}
