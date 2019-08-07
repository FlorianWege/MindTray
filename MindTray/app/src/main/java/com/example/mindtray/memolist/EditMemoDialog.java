package com.example.mindtray.memolist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import com.example.mindtray.R;
import com.example.mindtray.memo.Memo;
import com.example.mindtray.shared.MyDialog;

import java.util.Calendar;
import java.util.GregorianCalendar;

/*
	dialog to edit a memo's name/date
 */

public class EditMemoDialog extends MyDialog {
	private View _view;
	
	private EditText _editText_name;
	private DatePicker _datePicker;

	private Button _button_ok;
	private Button _button_cancel;
	
	private Memo _memo;
	
	void setMemo(Memo memo) {
		_memo = memo;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		_view = inflater.inflate(R.layout.dialog_edit_memo, container, false);

		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		_editText_name = (EditText) _view.findViewById(R.id.editText_name);

		_editText_name.selectAll();
		_editText_name.requestFocus();

		_editText_name.setText(_memo.getName());

		_datePicker = (DatePicker) _view.findViewById(R.id.datePicker);

		Calendar date = _memo.getDate();

		if (date != null) {
			_datePicker.updateDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
		}
		
		_button_ok = (Button) _view.findViewById(R.id.button_add);

		_button_ok.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				String name = null;
				
				if (!_editText_name.getText().toString().isEmpty()) {
					name = _editText_name.getText().toString();
				}
				
				if (name == null) {
					_editText_name.setError(getString(R.string.new_memo_error_no_name));
					_editText_name.requestFocus();
					
					return;
				}

				Calendar date = new GregorianCalendar(_datePicker.getYear(), _datePicker.getMonth(), _datePicker.getDayOfMonth());

				_memo.setName(name);
				_memo.setDate(date);
				
				dismiss();
			}
		});
		
		_button_cancel = (Button) _view.findViewById(R.id.button_cancel);

		_button_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});

		return _view;
	}
}