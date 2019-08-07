package com.example.mindtray.memo;

import com.example.mindtray.R;
import com.example.mindtray.shared.Storage;
import com.example.mindtray.shared.Util;

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

/*
	ContentFragment displaying an ImageContent
 */

public class ImageContentFragment extends ContentFragment {
	private ImageContent _memoContent;

	private EditText _editText;
	private ImageView _imageView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_image_content, container, false);

		//content label at the bottom
		TextView testView = (TextView) view.findViewById(R.id.textView_memoContent);

		testView.setText(_memoContent.getName());
		testView.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/AlexBrush-Regular-OTF.otf"));

		_editText = (EditText) view.findViewById(R.id.editText);

		_editText.setText(_memoContent.getText());

		//update storage when text changes
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

		//display image of model class
		_imageView = (ImageView) view.findViewById(R.id.imageView);
		
		_imageView.setImageBitmap(_memoContent.getBitmap());
		
		return view;
	}

	//setArgs because fragment constructor should be empty
	public void setArgs(ImageContent memoContent) {
		super.setArgs(memoContent);

		_memoContent = memoContent;
	}

	public ImageContentFragment() {
	}
}