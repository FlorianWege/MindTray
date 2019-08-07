package com.example.mindtray;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MemoAdapter extends ArrayAdapter<Memo> {

	public MemoAdapter(Context context, ArrayList<Memo> objects) {
		super(context, android.R.layout.simple_list_item_1);
	}

	public View getView(int pos, View convertView, ViewGroup parent) {		
		Memo memo = getItem(pos);
		
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_memos_listview, parent, false);
		}
		
		TextView nameView = (TextView) convertView.findViewById(R.id.textView_name);
		
		nameView.setText(memo.toString());
		
		return convertView;
	}
}
