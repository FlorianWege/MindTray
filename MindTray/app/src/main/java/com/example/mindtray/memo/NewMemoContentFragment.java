package com.example.mindtray.memo;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.Vector;

import com.example.mindtray.R;
import com.example.mindtray.shared.Storage;
import com.example.mindtray.shared.Util;

import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

public class NewMemoContentFragment extends Fragment {
	private Memo _memo;
	
	private TextView _textView_name;
	private EditText _editText_name;
	
	private TextView _textView_text;
	private EditText _editText_text;
	
	private RadioGroup _radioGroup_type;
	private ViewFlipper _viewFlipper;
	
	//image
	private Button _button_loadFromGallery;
	private Button _button_takePicture;
	private Bitmap _image_bitmap;
	private ImageView _imageView;
	
	private final int IMG_PICK_CODE = 100;
	private final int TAKE_PIC_CODE = 101;
	
	//audio
	private ToggleButton _button_record;
	private ToggleButton _button_play;
	
	private boolean _audio_isPlaying = false;
	private Thread _audio_play_updateThread;
	private AudioRecord _audio_recorder;
	private boolean _audio_isRecording = false;
	private MediaPlayer _audio_player;
	private File _audio_file = null;
	private LinearLayout _audio_layout;
	private View _audio_imageView;
	private OutputStream _audio_outStream;
	
	private final int AUDIO_SAMPLING_RATE = 8000;
	private final Queue<Short> _audio_lastData = new ArrayDeque<>();
	private final Vector<Short> _audio_totalData = new Vector<>();
	
	//
	private Button _button_addContent;
	
	private synchronized void addAudioData(short[] buffer, int len) {
		for (int i = 0; i < len; i++) {
			_audio_lastData.poll();
			_audio_lastData.add(buffer[i]);
			_audio_totalData.add(buffer[i]);
		}
	}

	//calculate points for canvas drawing (limited window)
	private synchronized float[] getAudioPts(float w, float h) {		
		final Iterator<Short> it = _audio_lastData.iterator();
		
		final int RESOLUTION = 2;
		
		final int ptsCount = AUDIO_SAMPLING_RATE / RESOLUTION;
		
		final float[] pts = new float[ptsCount*4];
		
		for (int i = 0; i < ptsCount; i++) {
			short val = it.next();
			
			if (i == 0) {
				pts[i*4] = 0;
				pts[i*4+1] = h / 2;
			} else {
				pts[i*4] = pts[(i-1)*4+2];
				pts[i*4+1] = pts[(i-1)*4+3];
			}
			
			pts[i*4+2] = ((float) i * w) / ptsCount;
			pts[i*4+3] = h * (1 - ((float) val - Short.MIN_VALUE) / (Short.MAX_VALUE - Short.MIN_VALUE));
			
			for (int j = 1; j < RESOLUTION; j++) {
				if (!it.hasNext()) break;
				
				it.next();
			}
		}
		
		return pts;
	}

	//calculate all points for canvas drawing
	private synchronized float[] getAudioTotalPts(float w, float h) {		
		final Iterator<Short> it = _audio_totalData.iterator();
		
		final int RESOLUTION = 2;
		
		final int ptsCount = _audio_totalData.size() / RESOLUTION;
		
		final float[] pts = new float[ptsCount*4];
		
		for (int i = 0; i < ptsCount; i++) {
			short val = it.next();
			
			if (i == 0) {
				pts[i*4] = 0;
				pts[i*4+1] = h / 2;
			} else {
				pts[i*4] = pts[(i-1)*4+2];
				pts[i*4+1] = pts[(i-1)*4+3];
			}
			
			pts[i*4+2] = ((float) i * w) / ptsCount;
			pts[i*4+3] = h * (1 - ((float) val - Short.MIN_VALUE) / (Short.MAX_VALUE - Short.MIN_VALUE));
			
			for (int j = 1; j < RESOLUTION; j++) {
				if (!it.hasNext()) break;
				
				it.next();
			}
		}
		
		return pts;
	}

	//header for wav files (Wikipedia...)
	private void writeWaveFileHeader(
			long totalAudioLen,
			long totalDataLen, long longSampleRate, int channels,
			long byteRate) throws IOException {
			 
			byte[] header = new byte[44];
			 
			header[0] = 'R'; // RIFF/WAVE header
			header[1] = 'I';
			header[2] = 'F';
			header[3] = 'F';
			header[4] = (byte) (totalDataLen & 0xff);
			header[5] = (byte) ((totalDataLen >> 8) & 0xff);
			header[6] = (byte) ((totalDataLen >> 16) & 0xff);
			header[7] = (byte) ((totalDataLen >> 24) & 0xff);
			header[8] = 'W';
			header[9] = 'A';
			header[10] = 'V';
			header[11] = 'E';
			header[12] = 'f'; // 'fmt ' chunk
			header[13] = 'm';
			header[14] = 't';
			header[15] = ' ';
			header[16] = 16; // 4 bytes: size of 'fmt ' chunk
			header[17] = 0;
			header[18] = 0;
			header[19] = 0;
			header[20] = 1; // format = 1
			header[21] = 0;
			header[22] = (byte) channels;
			header[23] = 0;
			header[24] = (byte) (longSampleRate & 0xff);
			header[25] = (byte) ((longSampleRate >> 8) & 0xff);
			header[26] = (byte) ((longSampleRate >> 16) & 0xff);
			header[27] = (byte) ((longSampleRate >> 24) & 0xff);
			header[28] = (byte) (byteRate & 0xff);
			header[29] = (byte) ((byteRate >> 8) & 0xff);
			header[30] = (byte) ((byteRate >> 16) & 0xff);
			header[31] = (byte) ((byteRate >> 24) & 0xff);
			header[32] = (byte) (2 * 16 / 8); // block align
			header[33] = 0;
			header[34] = 16; // bits per sample
			header[35] = 0;
			header[36] = 'd';
			header[37] = 'a';
			header[38] = 't';
			header[39] = 'a';
			header[40] = (byte) (totalAudioLen & 0xff);
			header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
			header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
			header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
			 
			_audio_outStream.write(header, 0, 44);
	}
	
	private void audio_startRecording() {
		_audio_file = new File(getActivity().getFilesDir(), "audioRecord.pcm");

		/*_audio_recorder = new MediaRecorder();
		
		_audio_recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		_audio_recorder.setAudioSamplingRate(AUDIO_SAMPLING_RATE);
		
		_audio_recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		
		_audio_recorder.setOutputFile(_audio_file.toString());
		
		_audio_recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		
		try {
			_audio_recorder.prepare();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		_audio_recorder.start();*/
		
		final short[] buffer = new short[4096];
		
		_audio_recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, AUDIO_SAMPLING_RATE, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, buffer.length);
		
		_audio_isRecording = true;
		_audio_recorder.startRecording();
		
		_audio_totalData.clear();
		_audio_imageView.postInvalidate();
		
		Util.printToast(getContext(), _audio_file.getAbsolutePath(), Toast.LENGTH_LONG);

		//update canvas in periodically
		Thread thread = new Thread() {
			@Override
			public void run() {
				while (_audio_isRecording) {					
					int len = _audio_recorder.read(buffer, 0, buffer.length);
					
					addAudioData(buffer, len);
						
					_audio_imageView.postInvalidate();
				}
			}
		};
		
		thread.start();
	}
	
	private void audio_stopRecording() {
		//recording stopped, reset everything
		_audio_recorder.stop();
		_audio_recorder.release();
		
		_audio_recorder = null;
		_audio_isRecording = false;

		try {
			//build output file from recorded bytes/shorts
			_audio_file.delete();
			
			_audio_outStream = new FileOutputStream(_audio_file);
			
			writeWaveFileHeader(_audio_totalData.size()*2, _audio_totalData.size()*2+44-8, AUDIO_SAMPLING_RATE, 1, 2);
			
			for (int i = 0; i < _audio_totalData.size(); i++) {
				_audio_outStream.write(_audio_totalData.get(i) & 0xff);
				_audio_outStream.write((_audio_totalData.get(i) >> 8) & 0xff);
			}
			
			_audio_outStream.close();
		} catch (Exception e) {
			Log.e(getClass().getSimpleName(), e.getMessage(), e);
			Util.printException(getContext(), e);
		}
	}

	//initial name for new content object to avoid duplicates
	private void updateDefaultName() {
		for (int i = 1; ; i++) {
			String contentName = "Content " + i;
			
			if (_memo.getContentByName(contentName) == null) {
				_editText_name.setHint(contentName);
				
				break;
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_new_memo_content, container, false);

		//name
		_textView_name = (TextView) view.findViewById(R.id.textView_name);
		_editText_name = (EditText) view.findViewById(R.id.editText_name);
		
		updateDefaultName();

		//text for all Content types
		_textView_text = (TextView) view.findViewById(R.id.textView_text);
		_editText_text = (EditText) view.findViewById(R.id.editText_text);

		//select special type
		_radioGroup_type = (RadioGroup) view.findViewById(R.id.radioGroup_type);
		_viewFlipper = (ViewFlipper) view.findViewById(R.id.viewFlipper);
		
		_radioGroup_type.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.radio_text: {
					_viewFlipper.setDisplayedChild(0);
					
					break;
				}
				case R.id.radio_image: {
					_viewFlipper.setDisplayedChild(1);
					
					break;
				}
				case R.id.radio_audio: {
					_viewFlipper.setDisplayedChild(2);
					
					break;
				}
				}
			}
		});
		
		_button_loadFromGallery = (Button) view.findViewById(R.id.button_loadFromGallery);
		
		_button_loadFromGallery.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//shared gallery on phone

				Intent imgPickerIntent = new Intent(Intent.ACTION_PICK);
				
				imgPickerIntent.setType("image/*");
				
				startActivityForResult(imgPickerIntent, IMG_PICK_CODE);
			}
		});
		
		_button_takePicture = (Button) view.findViewById(R.id.button_takePicture);
		
		_button_takePicture.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//call phone's camera

				Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				
				startActivityForResult(camIntent, TAKE_PIC_CODE);
			}
		});
		
		_imageView = (ImageView) view.findViewById(R.id.imageView);

		_button_addContent = (Button) view.findViewById(R.id.button_addContent);
		
		_button_addContent.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Content newMemoContent = null;

				String name = _editText_name.getText().toString();
				
				if ((name == null) || name.isEmpty()) name = _editText_name.getHint().toString();
				
				if (_memo.getContentByName(name) != null) {
					_editText_name.setError("Name already exists"); //TODO: why no text here?
					_editText_name.requestFocus();
					
					return;
				};

				//depending on the special type, create the Content and pass data
				switch (_radioGroup_type.getCheckedRadioButtonId()) {
				case R.id.radio_text: {
					try {
						TextContent textContent = new TextContent(name, _editText_text.getText().toString());

						newMemoContent = textContent;
					} catch (Storage.StorageException e) {
						Util.printException(getActivity(), e);
					}
					
					break;
				}
				case R.id.radio_image: {
					try {
						ImageContent imageContent = new ImageContent(name, ((BitmapDrawable) _imageView.getDrawable()).getBitmap());

						imageContent.setText(_editText_text.getText().toString());

						newMemoContent = imageContent;
					} catch (Storage.StorageException e) {
						Util.printException(getActivity(), e);
					}
					
					break;
				}
				case R.id.radio_audio: {
					try {
						ByteArrayOutputStream outStream = new ByteArrayOutputStream();
						InputStream inStream = new FileInputStream(_audio_file);

						byte[] buffer = new byte[4096];
						int read = 0;

						while ((read = inStream.read(buffer)) != -1) {
							outStream.write(buffer, 0, read);
						}

						inStream.close();
						outStream.close();

						AudioContent audioContent = new AudioContent(name, outStream.toByteArray());

						audioContent.setText(_editText_text.getText().toString());

						newMemoContent = audioContent;
					} catch (Exception e) {
						Util.printException(getActivity(), e);
					}
					
					break;
				}
				}

				//add new content to memo
				if (newMemoContent != null) {
					try {
						_memo.addContent(newMemoContent);
					} catch (Storage.StorageException e) {
						Util.printException(getActivity(), e);
					}

					updateDefaultName();
				}
			}
		});
		
		//audio
		for (int i = 0; i < AUDIO_SAMPLING_RATE; i++) {
			_audio_lastData.add((short) 0);
		}
		
		_button_record = (ToggleButton) view.findViewById(R.id.button_record);
		
		_button_record.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				_button_play.setEnabled(!isChecked);
				
				if (isChecked) {
					audio_startRecording();
				} else {
					audio_stopRecording();
				}
			}
		});
		
		_button_play = (ToggleButton) view.findViewById(R.id.button_play);
		
		_button_play.setEnabled(false);
		
		_button_play.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				_button_record.setEnabled(!isChecked);

				if (isChecked) {
					_audio_imageView.postInvalidate();
					
					if (_audio_file == null) return;

					_audio_player = new MediaPlayer();
					
					try {
						_audio_player.setDataSource(_audio_file.toString());
					} catch (Exception e) {
						Log.e(getClass().getSimpleName(), e.getMessage(), e);
						Util.printException(getActivity(), e);
					}
					
					try {
						_audio_player.prepare();
					} catch (Exception e) {
						Log.e(getClass().getSimpleName(), e.getMessage(), e);
						Util.printException(getActivity(), e);
					}
					
					_audio_player.start();
					_audio_isPlaying = true;
					
					_audio_play_updateThread = new Thread() {
						@Override
						public void run() {
							while (true) {
								_audio_imageView.postInvalidate();
								
								try {
									Thread.sleep(1);
								} catch (InterruptedException e) {
								}
							}
						}
					};
					
					_audio_play_updateThread.start();
					
					_audio_player.setOnCompletionListener(new OnCompletionListener() {
						@Override
						public void onCompletion(MediaPlayer mp) {
							_button_play.setChecked(false);
						}
					});
				} else {
					_audio_player.stop();
					_audio_player.release();
					
					_audio_player = null;
					
					_audio_isPlaying = false;
					_audio_play_updateThread.interrupt();
					
					_audio_imageView.postInvalidate();
				}
			}
		});
		
		_audio_layout = (LinearLayout) view.findViewById(R.id.layout);
		
		_audio_imageView = new View(getContext()) {
			@Override
			public void onDraw(Canvas canvas) {
				Paint paint = new Paint();
				
				canvas.drawRGB(0, 0, 0);
				
				if (_audio_isRecording) paint.setARGB(255, 0, 255, 0); else paint.setARGB(255, 255, 0, 0);
				
				paint.setStrokeWidth(3);
				
				int h = canvas.getHeight();
				int w = canvas.getWidth();
				
				canvas.drawLine(0, h / 2, w, h / 2, paint);
				
				float[] pts = (_audio_isRecording) ? getAudioPts(w, h) : getAudioTotalPts(w, h);
				
				canvas.drawLines(pts, paint);
				
				if (_audio_isPlaying) {
					int curX = (int) (w * ((float) _audio_player.getCurrentPosition()) / _audio_player.getDuration());
					
					Paint curPaint = new Paint();
					
					curPaint.setStrokeWidth(1);
					curPaint.setARGB(255, 0, 0, 255);
					
					canvas.drawLine(curX, 0, curX, h, curPaint);
				}
			}
		};
		
		_audio_imageView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		_audio_layout.addView(_audio_imageView);
		
		return view;
	}
	
	/*public Uri getImageUri(Context inContext, Bitmap inImage) {
	    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
	    inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
	    String path = Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
	    return Uri.parse(path);
	}

	public String getRealPathFromURI(Uri uri) {
	    Cursor cursor = getContentResolver().query(uri, null, null, null, null); 
	    cursor.moveToFirst(); 
	    int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA); 
	    return cursor.getString(idx); 
	}*/
	
	public String getOriginalImagePath() {
        String[] projection = { MediaStore.Images.Media.DATA };
        
        Cursor cursor = getActivity().managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, null);

        int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

		if (column_index_data < 0) {
			return "invalid";
		}

        cursor.moveToLast();

        return cursor.getString(column_index_data);
    }
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent valIntent) {
		super.onActivityResult(requestCode, resultCode, valIntent);
		
		if (valIntent == null) return;
		
		switch (requestCode) {
		case IMG_PICK_CODE: {
			Uri img = valIntent.getData();

			try {
				_imageView.setImageBitmap(null);
				
				_image_bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), img);
				
				BitmapFactory.Options options = new BitmapFactory.Options();
				
				options.inSampleSize = 4;
				
				InputStream in = getActivity().getContentResolver().openInputStream(img);
				
				Bitmap displayedBitmap = BitmapFactory.decodeStream(in, null, options);
				
				in.close();
				
				//int[] maxSize = new int[2];
				
				//GLES10.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxSize, 0);
				
				boolean resized = false;
				
				final int maxSize = 1024;
				
				if (displayedBitmap.getWidth() > maxSize) {
					double ratio = (double) displayedBitmap.getWidth() / displayedBitmap.getHeight();
					
					displayedBitmap = Bitmap.createScaledBitmap(displayedBitmap, maxSize, (int) (maxSize / ratio), false);
					resized = true;
				}
				
				if (displayedBitmap.getHeight() > maxSize) {
					double ratio = (double) displayedBitmap.getHeight() / displayedBitmap.getWidth();
					Log.e(getClass().getSimpleName(), "ratio " + ratio + ";" + maxSize + ";" + (int) (maxSize / ratio));
					displayedBitmap = Bitmap.createScaledBitmap(displayedBitmap, (int) (maxSize / ratio), maxSize, false);
					resized = true;
				}
				
				//Log.e("return", maxSize[0]+";"+maxSize[1]);
				_imageView.setImageBitmap(displayedBitmap);
				
				if (resized) {
					//Toast.makeText(getContext(), String.format("Resized to w=%d h=%d", _image_bitmap.getWidth(), _image_bitmap.getHeight()), Toast.LENGTH_LONG).show();
				}
			} catch (FileNotFoundException e) {
				Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
			} catch (Exception e) {
				Log.e(getClass().getSimpleName(), "exception", e);
				Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
			}
			
			break;
		}
		case TAKE_PIC_CODE: {
			Bitmap picBitmap = (Bitmap) valIntent.getExtras().get("data");
			Log.d("set pic", getOriginalImagePath());
			
			//_imageView.setImageURI(Uri.fromFile(new File(getOriginalImagePath())));
			_imageView.setImageBitmap(picBitmap);
			
			break;
		}
		}
	}

	public void setArgs(Memo memo) {
		_memo = memo;
	}

	public NewMemoContentFragment() {
	}
}