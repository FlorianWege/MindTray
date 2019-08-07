package com.example.mindtray.memo;

import com.example.mindtray.R;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class NewMemoDialogFragment extends DialogFragment {
	private View _view;
	
	private EditText _nameEdit;
	private Button _btnAdd;
	private Button _cancelAdd;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		_view = inflater.inflate(R.layout.dialogfragment_new_memo, container, false);
		
		_btnAdd = (Button) _view.findViewById(R.id.addBtn);
		
		_btnAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				String content = null;

				EditText nameText = (EditText) _view.findViewById(R.id.editText_name);
				
				if (!nameText.getText().toString().isEmpty()) {
					content = nameText.getText().toString();
				}
				
				if (content == null) {
					Toast.makeText(getActivity(), "Need a name", Toast.LENGTH_LONG).show();
					
					return;
				}
				
				((MemoListActivity) getActivity()).onFinishSetText(content);
				
				dismiss();
			}
		});
		
		_cancelAdd = (Button) _view.findViewById(R.id.cancelBtn);

		_cancelAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				dismiss();
			}
		});
		
		_nameEdit = (EditText) _view.findViewById(R.id.editText_name);

		_nameEdit.requestFocus();
		_nameEdit.selectAll();
		
		getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		
		imm.showSoftInput(_nameEdit, InputMethodManager.SHOW_IMPLICIT);

		return _view;
	}
	
	@Override
	public void show(FragmentManager manager, String tag) {
		super.show(manager, tag);
		
		//_nameEdit.selectAll();
	}
}
