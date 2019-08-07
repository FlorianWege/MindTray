package com.example.mindtray.memo;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.mindtray.R;
import com.example.mindtray.shared.Storage;
import com.example.mindtray.shared.Util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Vector;

/*
	ContentFragment displaying an AudioContent
 */

public class AudioContentFragment extends ContentFragment {
	private AudioContent _memoContent;

	private EditText _editText;
	private FrameLayout _layout_player;
	private View _imageView;
	private ToggleButton _button_play;

	private boolean _isPlaying = false;
	private Thread _play_updateThread;
	private MediaPlayer _player;
	private File _file;

	private final Vector<Short> _audio_totalData = new Vector<>();

	//build audio data (shorts) in a list
	private synchronized void addAudioData(short[] buffer) {
		for (int i = 0; i < buffer.length; i++) {
			_audio_totalData.add(buffer[i]);
		}
	}

	//creates an array of points used in canvas line drawing
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

	private OutputStream _outStream;

	//header for the wave (RIFF) file format
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

		_outStream.write(header, 0, 44);
	}

	//fixed sampling rate, in accordance to the recording sampling rate
	private final int AUDIO_SAMPLING_RATE = 8000;

	//make a file from the audio date in order to deliver it to the media player
	private void makeFile() throws Exception {
		_file = new File(getActivity().getFilesDir(), "audioPlay.pcm");

		try {
			_file.delete();

			_outStream = new FileOutputStream(_file);

			//make header first
			writeWaveFileHeader(_audio_totalData.size()*2, _audio_totalData.size()*2+44-8, AUDIO_SAMPLING_RATE, 1, 2);

			//add bytes/fetch the single bytes from the shorts
			for (int i = 0; i < _audio_totalData.size(); i++) {
				_outStream.write(_audio_totalData.get(i) & 0xff);
				_outStream.write((_audio_totalData.get(i) >> 8) & 0xff);
			}

			_outStream.close();
		} catch (Exception e) {
			throw e;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_audio_content, container, false);

		//memo label at the bottom
		TextView testView = (TextView) view.findViewById(R.id.textView_memoContent);

		testView.setText(_memoContent.getName());
		testView.setTypeface(Typeface.createFromAsset(getActivity().getAssets(), "fonts/AlexBrush-Regular-OTF.otf"));

		//edit text
		_editText = (EditText) view.findViewById(R.id.editText);

		_editText.setText(_memoContent.getText());

		//need to watch the text in order to adjust the storage side
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

		//subclass View and draw our own canvas
		_imageView = new View(getContext()) {
			@Override
			public void onDraw(Canvas canvas) {
				//avoid allocating a new Paint object everytime...
				Paint paint = new Paint();

				canvas.drawRGB(0, 0, 0);

				paint.setARGB(255, 255, 0, 0);

				paint.setStrokeWidth(3);

				int h = canvas.getHeight();
				int w = canvas.getWidth();

				//draw horizontal line
				canvas.drawLine(0, h / 2, w, h / 2, paint);

				float[] pts = getAudioTotalPts(w, h);

				//draw amplitudes
				canvas.drawLines(pts, paint);

				if (_isPlaying) {
					//draw current position marker when playing
					int curX = (int) (w * ((float) _player.getCurrentPosition()) / _player.getDuration());

					Paint curPaint = new Paint();

					curPaint.setStrokeWidth(1);
					curPaint.setARGB(255, 0, 0, 255);

					canvas.drawLine(curX, 0, curX, h, curPaint);
				}
			}
		};

		_layout_player = (FrameLayout) view.findViewById(R.id.layout_player);

		//add our custom view to its designed box and postInvalidate once in order to force drawing
		_layout_player.addView(_imageView);
		_imageView.postInvalidate();

		_button_play = (ToggleButton) view.findViewById(R.id.button_play);

		_button_play.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					//reset drawing
					_imageView.postInvalidate();

					//prepare file for player
					try {
						makeFile();
					} catch (Exception e) {
						Util.printException(getActivity(), e);
					}

					if (_file == null) return;

					//prepare player
					_player = new MediaPlayer();

					try {
						_player.setDataSource(_file.toString());
					} catch (Exception e) {
						Log.e(getClass().getSimpleName(), e.getMessage(), e);
						Util.printException(getActivity(), e);
					}

					try {
						_player.prepare();
					} catch (Exception e) {
						Log.e(getClass().getSimpleName(), e.getMessage(), e);
						Util.printException(getActivity(), e);
					}

					//play
					_player.start();
					_isPlaying = true;

					//extra thread updates the canvas
					_play_updateThread = new Thread() {
						@Override
						public void run() {
							while (true) {
								_imageView.postInvalidate();

								try {
									Thread.sleep(1);
								} catch (InterruptedException e) {
								}
							}
						}
					};

					//do not forget to start thread...
					_play_updateThread.start();

					//reset button when player finished playing
					_player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
						@Override
						public void onCompletion(MediaPlayer mp) {
							_button_play.setChecked(false);
						}
					});
				} else {
					//reset everything when stop is pressed
					_player.stop();
					_player.release();

					_player = null;

					_isPlaying = false;
					_play_updateThread.interrupt();

					_imageView.postInvalidate();
				}
			}
		});

		//convert raw bytes to shordt and pass it to the audio data data structure
		byte[] bArr = _memoContent.getBytes();

		short[] sArr = new short[bArr.length / 2];

		for (int i = 0; i < sArr.length; i++) {
			//sArr[i] = (short) ((bArr[i*2] << 8) + (bArr[i*2+1]));
			sArr[i] = (short) ((bArr[i*2]) + (bArr[i*2+1] << 8));
		}

		addAudioData(sArr);

		return view;
	}

	//setArgs because Fragment constructor should be empty
	public void setArgs(AudioContent memoContent) {
		super.setArgs(memoContent);

		_memoContent = memoContent;
	}

	public AudioContentFragment() {
	}
}