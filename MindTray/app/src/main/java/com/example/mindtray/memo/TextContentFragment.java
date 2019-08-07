package com.example.mindtray.memo;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mindtray.R;
import com.example.mindtray.shared.Storage;
import com.example.mindtray.shared.Util;

public class TextContentFragment extends ContentFragment {
	private TextContent _memoContent;

	private EditText _editText;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_text_content, container, false);

		//content label at the bottom
		TextView testView = (TextView) view.findViewById(R.id.textView_memoContent);

		testView.setText(_memoContent.getName());
		testView.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/AlexBrush-Regular-OTF.otf"));

		_editText = (EditText) view.findViewById(R.id.editText);

		_editText.setText(_memoContent.getText());

		//equalize storage when text changes
		_editText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				try {
					_memoContent.setText(s.toString());
				} catch (Storage.StorageException e) {
					Util.printException(getActivity(), e);
				}
			}
		});

		return view;
	}

	//setArgs because fragment constructor should be empty
	public void setArgs(TextContent memoContent) {
		super.setArgs(memoContent);

		_memoContent = memoContent;
	}

	public TextContentFragment() {
	}
}