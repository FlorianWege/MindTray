package com.example.mindtray;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class NewMemoDialogFragment extends DialogFragment {
	View _view;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		_view = inflater.inflate(R.layout.dialogfragment_new_memo, container, false);
		
		Button btnAdd = (Button) _view.findViewById(R.id.button_add);
		
		btnAdd.setOnClickListener(new OnClickListener() {
			public void onClick(View view) {
				String content = null;

				EditText nameText = (EditText) _view.findViewById(R.id.editText_name);
				
				if (nameText.getText().toString() != "") {
					content = nameText.getText().toString();
				}
				
				((MemoListActivity) getActivity()).onFinishSetText(content);
				
				dismiss();
			}
			
		});

		return _view;
	}
}
