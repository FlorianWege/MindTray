package com.example.mindtray.memo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Queue;
import java.util.Vector;

import com.example.mindtray.R;

import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Debug;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;

public class NewMemoContentFragment extends Fragment {
	private Activity _parentActivity;
	private Memo _memo;
	
	private RadioGroup _typeRadioGroup;
	private ViewFlipper _viewFlipper;
	
	private Button _grabFromGalleryBtn;
	private Button _takePictureBtn;
	private ImageView _imageView;
	
	private final int IMG_PICK_CODE = 100;
	private final int TAKE_PIC_CODE = 101;
	
	private ToggleButton _audio_recordBtn;
	private ToggleButton _audio_playBtn;
	private boolean _audio_isPlaying = false;
	private Thread _audio_play_updateThread;
	private AudioRecord _audio_recorder;
	private boolean _audio_isRecording = false;
	private MediaPlayer _audio_player;
	private File _audio_file = null;
	private LinearLayout _audio_layout;
	private View _audio_imageView;
	private OutputStream _audio_outStream;
	
	private Button _addContentBtn;
	
	private final int AUDIO_SAMPLING_RATE = 8000;
	private final Queue<Short> _audio_lastData = new ArrayDeque<Short>();
	private final Vector<Short> _audio_totalData = new Vector<Short>();
	
	private synchronized void addAudioData(short[] buffer, int len) {
		for (int i = 0; i < len; i++) {
			_audio_lastData.poll();
			_audio_lastData.add(buffer[i]);
			_audio_totalData.add(buffer[i]);
		}
	}
	
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
		
		Toast.makeText(getContext(), _audio_file.getAbsolutePath(), Toast.LENGTH_LONG).show();;
		
		Log.e("record", "start");
		
		Thread thread = new Thread() {
			@Override
			public void run() {
				Log.e("thread", "start");
				
				while (_audio_isRecording) {					
					int len = _audio_recorder.read(buffer, 0, buffer.length);
					
					addAudioData(buffer, len);
						
					_audio_imageView.postInvalidate();
					
					//Log.e("record", "stream empty");
					//Thread.sleep(100);
				}
				
				Log.e("record", "stream ended");
			}
		};
		
		thread.start();
	}
	
	private void audio_stopRecording() {
		_audio_recorder.stop();
		_audio_recorder.release();
		
		_audio_recorder = null;
		_audio_isRecording = false;

		try {
			_audio_file.delete();
			
			_audio_outStream = new FileOutputStream(_audio_file);
			
			writeWaveFileHeader(_audio_totalData.size()*2, _audio_totalData.size()*2+44-8, AUDIO_SAMPLING_RATE, 1, 2);
			
			for (int i = 0; i < _audio_totalData.size(); i++) {
				_audio_outStream.write(_audio_totalData.get(i) & 0xff);
				_audio_outStream.write((_audio_totalData.get(i) >> 8) & 0xff);
			}
			
			_audio_outStream.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_new_memo_content, container, false);
		
		_typeRadioGroup = (RadioGroup) view.findViewById(R.id.typeRadioGroup);
		_viewFlipper = (ViewFlipper) view.findViewById(R.id.viewFlipper);
		
		_typeRadioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.textRadio: {
					_viewFlipper.setDisplayedChild(0);
					
					break;
				}
				case R.id.imageRadio: {
					_viewFlipper.setDisplayedChild(1);
					
					break;
				}
				case R.id.audioRadio: {
					_viewFlipper.setDisplayedChild(2);
					
					break;
				}
				}
			}
		});
		
		_grabFromGalleryBtn = (Button) view.findViewById(R.id.image_grabFromGalleryBtn);
		
		_grabFromGalleryBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent imgPickerIntent = new Intent(Intent.ACTION_PICK);
				
				imgPickerIntent.setType("image/*");
				
				startActivityForResult(imgPickerIntent, IMG_PICK_CODE);
			}
		});
		
		_takePictureBtn = (Button) view.findViewById(R.id.image_takePictureBtn);
		
		_takePictureBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//File outDir = new File(Environment.);
				
				//File outFile = new File();
				
				Intent camIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				
				//camIntent.putExtra(MediaStore.EXTRA_OUTPUT, outFile);
				
				startActivityForResult(camIntent, TAKE_PIC_CODE);
			}
		});
		
		_imageView = (ImageView) view.findViewById(R.id.image_imageView);

		_addContentBtn = (Button) view.findViewById(R.id.addContentBtn);
		
		_addContentBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				MemoContent newMemoContent = null;
				
				switch (_typeRadioGroup.getCheckedRadioButtonId()) {
				case R.id.textRadio: {
					newMemoContent = new TextContent("abc");
					
					break;
				}
				case R.id.imageRadio: {
					Log.d("createimg", ((BitmapDrawable) _imageView.getDrawable()).getBitmap().toString());
					newMemoContent = new ImageContent("def", ((BitmapDrawable) _imageView.getDrawable()).getBitmap());
					
					break;
				}
				case R.id.audioRadio: {
					newMemoContent = new AudioContent("ghi");
					
					break;
				}
				}
				
				if (newMemoContent == null) return;
				
				_memo.addContent(newMemoContent);
			}
		});
		
		//audio
		for (int i = 0; i < AUDIO_SAMPLING_RATE; i++) {
			_audio_lastData.add((short) 0);
		}
		
		_audio_recordBtn = (ToggleButton) view.findViewById(R.id.audio_recordBtn);
		
		_audio_recordBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				_audio_playBtn.setEnabled(!isChecked);
				
				if (isChecked) {
					audio_startRecording();
				} else {
					audio_stopRecording();
				}
			}
		});
		
		_audio_playBtn = (ToggleButton) view.findViewById(R.id.audio_playBtn);
		
		_audio_playBtn.setEnabled(false);
		
		_audio_playBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				_audio_recordBtn.setEnabled(!isChecked);

				if (isChecked) {
					_audio_imageView.postInvalidate();
					
					if (_audio_file == null) return;

					_audio_player = new MediaPlayer();
					
					try {
						_audio_player.setDataSource(_audio_file.toString());
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SecurityException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					try {
						_audio_player.prepare();
					} catch (IllegalStateException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
							_audio_playBtn.setChecked(false);
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
		
		_audio_layout = (LinearLayout) view.findViewById(R.id.audio_layout);
		
		Log.e("layout", _audio_layout.toString());
		Log.e("layoutB", getContext().toString());
		
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
        Cursor cursor = getActivity().managedQuery(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, null, null, null);
        int column_index_data = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
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
			Log.d("return", img.toString());
			_imageView.setImageURI(img);
			
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
	
	public NewMemoContentFragment(Memo memo) {
		_memo = memo;
	}
}
