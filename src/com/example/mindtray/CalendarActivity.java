package com.example.mindtray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import com.example.mindtray.memo.MemoListActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CalendarActivity extends Activity {
	private LinearLayout _layout_root;
	private LinearLayout _calendarLayout;
	private LinearLayout _calendarHeaderLayout;
	private LinearLayout _calendarBodyLayout;

	private CustomCalendarView _calendarView;
	
	private Button _button_decMonth;
	private Button _button_incMonth;
	
	private TextView _textView_title;
	
	private Button _button_back;
	
	private static class CustomCalendarView extends View {
		private Calendar _cal;
		private int _selectedDay = 0;
		
		private final int _rowC = 6;
		private final int _colC = 7;
		
		private int getDayAtCoords(float x, float y) {
			int w = getWidth();
			int h = getHeight();

			int rowH = h / _rowC;
			int colW = w / _colC;
			
			if (y < rowH) {				
				return 0;
			}
			
			int col = ((int) x) / colW;
			int row = ((int) y) / rowH - 1;
			
			int index = row * _colC + col;
			
			_cal.setFirstDayOfWeek(Calendar.MONDAY);
			_cal.set(Calendar.DAY_OF_MONTH, _cal.getActualMinimum(Calendar.DAY_OF_MONTH));
			
			int offset = _cal.get(Calendar.DAY_OF_WEEK) - 1;
			
			if (offset == 0) offset = 6; else offset = offset - 1;
			
			index -= offset;
			
			if (index < _cal.getActualMinimum(Calendar.DAY_OF_MONTH) - 1) return 0;
			if (index > _cal.getActualMaximum(Calendar.DAY_OF_MONTH) - 1) return 0;
			
			return index + 1;
		}
		
		private final Rect _textBounds = new Rect();
		
		private final Paint _bgPaint = new Paint();
		private final Paint _dateTextPaint = new Paint();
		private final Paint _linePaint = new Paint();
		private Paint _memoTextPaint = new Paint();
		private final Paint _selectedBGPaint = new Paint();
		private Paint _selectedTextPaint = new Paint();
		
		private int _w = 0;
		private int _h = 0;
		private int _colW = 0;
		private int _rowH = 0;
		
		private LinearGradient _selectedShader = null;
		private LinearGradient _memoDatesShader = null;
		
		Calendar _paintCal = new GregorianCalendar();
		
		private void updateSize(int w, int h) {
			if (w != _w || h != _h) {
				_w = w;
				_h = h;

				_rowH = _h / _rowC;
				_colW = _w / _colC;
				Log.e("updateSizeA", _rowH + ";" + _h);
				Log.e("updateSizeB", _colW + ";" + _w);
				_dateTextPaint.setTextSize(_rowH);
				_dateTextPaint.setTextScaleX(((float) _colW) / _rowH / 3);
				
				_linePaint.setARGB(255, 0, 0, 0);
				
				_selectedShader = new LinearGradient(0, 0, _colW / 2, _rowH / 2, Color.YELLOW, Color.YELLOW, Shader.TileMode.MIRROR);
				_memoDatesShader = new LinearGradient(0, 0, _colW / 2, _rowH / 2, Color.YELLOW, Color.RED, Shader.TileMode.MIRROR);
				
				_selectedBGPaint.setARGB(127, 255, 0, 0);
				_selectedBGPaint.setShader(_selectedShader);
				
				_selectedTextPaint = new Paint(_dateTextPaint);
				
				//_selectedTextPaint.setColor(Color.argb(255, 255, 255, 255));
				_selectedTextPaint.setFakeBoldText(true);
				//_selectedTextPaint.setTextAlign(Align.CENTER);
				_selectedTextPaint.setTextSize(_rowH);
				_selectedTextPaint.setTextScaleX((float) (_dateTextPaint.getTextScaleX() * 0.75));
				_selectedTextPaint.setColor(Color.WHITE);
				
				_memoTextPaint = new Paint(_dateTextPaint);
				
				//_memoTextPaint.setColor(Color.argb(255, 255, 255, 255));
				_memoTextPaint.setFakeBoldText(true);
				//_memoTextPaint.setTextAlign(Align.CENTER);
				_memoTextPaint.setTextSize(_rowH);
				_memoTextPaint.setTextScaleX((float) (_dateTextPaint.getTextScaleX() * 0.75));
				_memoTextPaint.setColor(Color.WHITE);
			} 
		}
		
		@Override
		public void onSizeChanged(int w, int h, int oldw, int oldh) {
			
		}
		
		@Override
		public void onDraw(Canvas canvas) {
			canvas.drawColor(_bgColorVal.data);
			
			int w = canvas.getClipBounds().width();
			int h = canvas.getClipBounds().height();
			//Log.e("canvasSize", w + ";" + h);
			updateSize(w, h);
			//Log.e("canvasSizeB", canvas.getClipBounds().toString());
			Paint monthTextPaint = _dateTextPaint;
			
			for (int i = 0; i < _colC; i++) {
				String text = "";
				
				switch (i) {
				case 0: {
					text = "Mon";
					
					break;
				}
				case 1: {
					text = "Tue";
					
					break;
				}
				case 2: {
					text = "Wed";
					
					break;
				}
				case 3: {
					text = "Thu";
					
					break;
				}
				case 4: {
					text = "Fri";
					
					break;
				}
				case 5: {
					text = "Sat";
					
					break;
				}
				case 6: {
					text = "Sun";
					
					break;
				}
				}
				
				if (!text.isEmpty()) {
					monthTextPaint.getTextBounds(text, 0, text.length(), _textBounds);
					
					//canvas.drawText(text, i * colW + colW / 2, rowH, monthTextPaint);
					
					float x = i * _colW + _colW / 2 - _textBounds.exactCenterX();
					float y = _rowH / 2 - _textBounds.exactCenterY();

					canvas.drawText(text, x, y, monthTextPaint);
				}
			}

			canvas.drawLine(0, _rowH, w, _rowH, _linePaint);
			
			_paintCal.setTime(_cal.getTime());
			
			int daysInMonth = _cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			
			_paintCal.setFirstDayOfWeek(Calendar.MONDAY);
			_paintCal.set(Calendar.DAY_OF_MONTH, _paintCal.getActualMinimum(Calendar.DAY_OF_MONTH));
			
			int offset = _paintCal.get(Calendar.DAY_OF_WEEK) - 1;
			
			if (offset == 0) offset = 6; else offset -= 1;
			
			for (int i = 0; i < daysInMonth; i++) {				
				int col = (i + offset) % _colC;
				int row = (i + offset) / _colC + 1;
				
				_paintCal.set(_paintCal.get(Calendar.YEAR), _paintCal.get(Calendar.MONTH), i, 0, 0, 0);
				
				//Date curDate = _paintCal.getTime();
				Paint curDateTextPaint = _dateTextPaint;
				
				String text = Integer.toString(i + 1);
				
				if ((i + 1) == _selectedDay) {					
					//canvas.drawRect(col * _colW, row * _rowH, (col + 1) * _colW - 1, row * _rowH + _rowH - 1, _selectedBGPaint);
					
					curDateTextPaint = _selectedTextPaint;
				} else {
					/*for (int j = 0; j < _memoDates.size(); j++) {
						Date date = _memoDates.get(j);
						
						if (date.getYear() == curDate.getYear() && date.getMonth() == curDate.getMonth() && date.getDate() == curDate.getDate()) {
							_bgPaint.setARGB(127, 255, 0, 0);
							_bgPaint.setShader(_memoDatesShader);
							
							canvas.drawRect(col * _colW, row * _rowH, (col + 1) * _colW - 1, row * _rowH + _rowH - 1, _bgPaint);
							
							curDateTextPaint = _memoTextPaint;
							
							break;
						}
					}*/
				}
				
				curDateTextPaint.getTextBounds(text, 0, text.length(), _textBounds);
				Log.e("paint", "draw text " + text + " at " + (row * _rowH + _rowH / 2) + ";" + _textBounds.exactCenterY());
				//canvas.drawText(text, col * colW + colW / 2, row * rowH + rowH, curDateTextPaint);
				canvas.drawText(text, col * _colW + _colW / 2 - _textBounds.exactCenterX(), row * _rowH + _rowH / 2 - _textBounds.exactCenterY(), curDateTextPaint);
			}
		}
		
		private TextView _textView_title;
		
		private void updateTitle() {
			/*Paint yearMonthPaint = new Paint(textPaint);
			
			yearMonthPaint.setTextAlign(Align.CENTER);
			yearMonthPaint.setTextSize(rowH);
			yearMonthPaint.setTextScaleX(((float) colW) / rowH / 3);
			yearMonthPaint.setFakeBoldText(true);*/
			
			String yearMonthLabel = String.format(Locale.US, "%d %s", _cal.get(Calendar.YEAR), _cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US));
			
			//canvas.drawText(yearMonthLabel, w / 2, rowH, yearMonthPaint);
			
			_textView_title.setText(yearMonthLabel);
		}
		
		private void setTitle(TextView val) {
			_textView_title = val;
			
			updateTitle();
		}
		
		public void decMonth() {
			if (_cal.get(Calendar.MONTH) == _cal.getActualMinimum(Calendar.MONTH)) {
				_cal.add(Calendar.YEAR, -1);
				_cal.set(Calendar.MONTH, _cal.getActualMaximum(Calendar.MONTH));
			} else {
				_cal.add(Calendar.MONTH, -1);
			}
			
			postInvalidate();
			updateTitle();
		}

		public void incMonth() {
			if (_cal.get(Calendar.MONTH) == _cal.getActualMaximum(Calendar.MONTH)) {
				_cal.add(Calendar.YEAR, 1);
				_cal.set(Calendar.MONTH, _cal.getActualMinimum(Calendar.MONTH));
			} else {
				_cal.add(Calendar.MONTH, 1);
			}
			
			postInvalidate();
			updateTitle();
		}
		
		public interface OnDateClickedListener {
			public void handle(Date date);
		}
		
		private List<OnDateClickedListener> _onDateClickedListeners = new ArrayList<OnDateClickedListener>();
		
		public void addOnDateClickedListener(OnDateClickedListener val) {
			_onDateClickedListeners.add(val);
		}
		
		private TypedValue _bgColorVal;
		
		@Override
		public boolean performClick() {
			return super.performClick();
		}
		
		private List<Date> _memoDates = new ArrayList<Date>();
		
		public CustomCalendarView(Context context) {
			super(context);
	
			_bgColorVal = new TypedValue();
			
			getContext().getTheme().resolveAttribute(android.R.attr.windowBackground, _bgColorVal, true);
			
			Date date = new Date();
			
			_cal = new GregorianCalendar();
			
			_cal.setTime(date);
			
			_dateTextPaint.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/AlexBrush-Regular-OTF.otf"));
			
			invalidate();
			
			setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {					
					int day = getDayAtCoords(event.getX(), event.getY());

					if (event.getAction() == MotionEvent.ACTION_UP) {
						_selectedDay = 0;
					} else {
						_selectedDay = day;
					}
					
					invalidate();
					
					if (day == 0) return true;
					if (event.getAction() != MotionEvent.ACTION_UP) return true;
					
					v.performClick();
					
					_cal.set(Calendar.DAY_OF_MONTH, day);
					
					Date date = _cal.getTime();
					
					for (OnDateClickedListener listener : _onDateClickedListeners) {
						listener.handle(date);
					}
					
					return true;
				}
			});
			
			_memoDates.add(new Date(2016-1900, 11, 20));
		}
	}
	
	private void startMemoList(Date date) {
		Intent intent = new Intent(this, MemoListActivity.class);
		
		intent.putExtra("date", date);
		
		startActivity(intent);
	}
	
	public void button_back_onClick(View view) {
		finish();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_calendar);
		
		_layout_root = (LinearLayout) findViewById(R.id.layout_root);
		
		_calendarLayout = (LinearLayout) findViewById(R.id.layout_calendar);
		
		_calendarHeaderLayout = (LinearLayout) findViewById(R.id.layout_calendar_header);
		_calendarBodyLayout = (LinearLayout) findViewById(R.id.layout_calendar_body);
		
		_calendarView = new CustomCalendarView(getApplicationContext());
		
		//_calendarView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		_calendarBodyLayout.addView(_calendarView);
		
		_calendarView.addOnDateClickedListener(new CustomCalendarView.OnDateClickedListener() {
			@Override
			public void handle(Date date) {
				startMemoList(date);
			}
		});
		
		_button_decMonth = (Button) findViewById(R.id.button_decMonth);
		
		_button_decMonth.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_calendarView.decMonth();
			}
		});
		
		_button_incMonth = (Button) findViewById(R.id.button_incMonth);

		_button_incMonth.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_calendarView.incMonth();
			}
		});
		
		_textView_title = (TextView) findViewById(R.id.textView_title);
		
		_calendarView.setTitle(_textView_title);
		
		_button_back = (Button) findViewById(R.id.button_back);
		
		_button_back.setVisibility(View.GONE);
	}
}
