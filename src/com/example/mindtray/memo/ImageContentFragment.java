package com.example.mindtray.memo;

import com.example.mindtray.R;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ImageContentFragment extends MemoContentFragment {
	private ImageContent _memoContent;
	
	private ImageView _imageView;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_image_content, container, false);
		
		//LinearLayout root = (LinearLayout) view.findViewById(R.id.memoContentRoot);
		
		/*_imageView = new ImageView(view.getContext());

		_imageView.setImageBitmap(_memoContent.getBitmap());
		
		root.addView(_imageView);*/
		
		_imageView = (ImageView) view.findViewById(R.id.imageView);
		
		_imageView.setImageBitmap(_memoContent.getBitmap());
		
		return view;
	}

	public ImageContentFragment(ImageContent memoContent) {
		super(memoContent);
		
		_memoContent = memoContent;
	}
}
