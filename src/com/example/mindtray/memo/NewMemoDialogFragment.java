package com.example.mindtray.memo;

import com.example.mindtray.R;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class NewMemoDialogFragment extends DialogFragment {
	private View _view;
	
	private EditText _editText_name;
	private CheckBox _checkBox_startMemo;
	private Button _button_add;
	private Button _button_cancel;
	
	private boolean _startMemoFlag = false;
	
	void setStartMemoFlag(boolean val) {
		_startMemoFlag = val;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		_view = inflater.inflate(R.layout.dialogfragment_new_memo, container, false);

		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		
		_editText_name = (EditText) _view.findViewById(R.id.editText_name);

		_editText_name.requestFocus();
		_editText_name.selectAll();
		
		getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		
		imm.showSoftInput(_editText_name, InputMethodManager.SHOW_IMPLICIT);
		
		_checkBox_startMemo = (CheckBox) _view.findViewById(R.id.checkBox_startMemo);
		
		_checkBox_startMemo.setChecked(_startMemoFlag);
		
		_button_add = (Button) _view.findViewById(R.id.button_add);
		
		_button_add.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				String content = null;
				
				if (!_editText_name.getText().toString().isEmpty()) {
					content = _editText_name.getText().toString();
				}
				
				if (content == null) {
					_editText_name.setError(getString(R.string.newmemo_error_no_name));
					_editText_name.requestFocus();
					
					return;
				}
				
				((MemoListActivity) getActivity()).onFinishSetText(content, _checkBox_startMemo.isChecked());
				
				dismiss();
			}
		});
		
		_button_add.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/AlexBrush-Regular-OTF.otf"));
		
		_button_cancel = (Button) _view.findViewById(R.id.button_cancel);

		_button_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});
		
		_button_cancel.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/AlexBrush-Regular-OTF.otf"));

		return _view;
	}
	
	@Override
	public void show(FragmentManager manager, String tag) {
		super.show(manager, tag);
		
		//_nameEdit.selectAll();
	}
}
