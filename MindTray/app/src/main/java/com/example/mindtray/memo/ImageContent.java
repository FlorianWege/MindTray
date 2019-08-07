package com.example.mindtray.memo;

import android.graphics.Bitmap;

public class ImageContent extends Content {
	//bitmap data for the image
	private Bitmap _bitmap;
	
	public Bitmap getBitmap() {
		return _bitmap;
	}
	
	public ImageContent(String name, Bitmap bitmap) {
		super(name);
		
		_bitmap = bitmap;
	}
}
