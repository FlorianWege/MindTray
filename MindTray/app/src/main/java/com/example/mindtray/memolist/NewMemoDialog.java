package com.example.mindtray.memolist;

import com.example.mindtray.R;
import com.example.mindtray.memo.Memo;
import com.example.mindtray.shared.MyDialog;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class NewMemoDialog extends MyDialog {
	private View _view;
	
	private EditText _editText_name;
	private DatePicker _datePicker;
	private CheckBox _checkBox_startMemo;

	private Button _button_add;
	private Button _button_cancel;

	public interface Listener {
		void onFail(Exception e);
		void onSuccess(Memo memo, boolean startMemo);
	}

	private boolean _startMemoFlag = false;
	private Listener _listener = null;
	
	void setArgs(boolean val, Listener listener) {
		_startMemoFlag = val;
		_listener = listener;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		_view = inflater.inflate(R.layout.dialog_new_memo, container, false);

		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		_editText_name = (EditText) _view.findViewById(R.id.editText_name);

		_editText_name.requestFocus();
		_editText_name.selectAll();
		
		getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		
		imm.showSoftInput(_editText_name, InputMethodManager.SHOW_IMPLICIT);

		_datePicker = (DatePicker) _view.findViewById(R.id.datePicker);

		_checkBox_startMemo = (CheckBox) _view.findViewById(R.id.checkBox_startMemo);
		
		_checkBox_startMemo.setChecked(_startMemoFlag);
		
		_button_add = (Button) _view.findViewById(R.id.button_add);
		
		_button_add.setOnClickListener(new OnClickListener() {
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

				Memo memo = new Memo(name, date);

				if (_listener != null) {
					_listener.onSuccess(memo, _checkBox_startMemo.isChecked());
				}

				dismiss();
			}
		});
		
		_button_cancel = (Button) _view.findViewById(R.id.button_cancel);

		_button_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				if (_listener != null) {
					_listener.onFail(null);
				}

				dismiss();
			}
		});

		return _view;
	}
}