package com.example.mindtray.memolist;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.example.mindtray.R;
import com.example.mindtray.memo.Memo;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.BaseAdapter;

public class MemoListAdapter extends BaseAdapter {
	private Context _context;
	private List<Memo> _items = new ArrayList<Memo>();

	public List<Memo> getItems() {
		return _items;
	}

	public void clear() {
		_items.clear();
	}

	public void add(Memo val) {
		_items.add(val);
	}

	public void remove(Memo val) {
		_items.remove(val);
	}

	public MemoListAdapter(Context context) {
		//super(context, android.R.layout.simple_list_item_2);

		_context = context;
	}

	@Override
	public int getCount() {
		return _items.size();
	}

	@Override
	public Memo getItem(int i) {
		return _items.get(i);
	}

	@Override
	public long getItemId(int i) {
		return 0;
	}

	@Override
	public View getView(int pos, View convertView, ViewGroup parent) {		
		Memo memo = getItem(pos);
		
		if (convertView == null) convertView = LayoutInflater.from(_context).inflate(R.layout.activity_memolist_item, parent, false);
		
		TextView nameView = (TextView) convertView.findViewById(R.id.textView_name);
		
		Typeface nameFont = Typeface.createFromAsset(_context.getAssets(), "fonts/AlexBrush-Regular-OTF.otf");
		
		nameView.setTypeface(nameFont);
		
		nameView.setText(memo.toString());

		TextView dateView = (TextView) convertView.findViewById(R.id.textView_date);

		Calendar date = memo.getDate();

		String dateS = new SimpleDateFormat(date.get(Calendar.YEAR) == Calendar.getInstance().get(Calendar.YEAR) ? "MM-dd" : "yyyy-MM-dd").format(date.getTime());

		dateView.setText(dateS);

		return convertView;
	}

	@Override
	public void notifyDataSetChanged() {
		Collections.sort(_items, new Comparator<Memo>() {
			@Override
			public int compare(Memo a, Memo b) {
				return a.getDate().compareTo(b.getDate());
			}
		});

		super.notifyDataSetChanged();
	}
}
