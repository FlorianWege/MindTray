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
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;

public class CalendarActivity extends Activity {
	private LinearLayout _rootLayout;
	private GridLayout _calendarLayout;

	private CustomCalendarView _calendarView;
	
	private Button _decMonthBtn;
	private Button _incMonthBtn;
	
	private static class CustomCalendarView extends View {
		private Calendar _cal;
		private int _selectedDay = 0;
		
		private int getDayAtCoords(float x, float y) {
			int w = getWidth();
			int h = getHeight();

			int rowH = h / 8;
			int colW = w / 7;
			
			if (y < rowH * 2) {				
				return 0;
			}
			
			int col = ((int) x) / colW;
			int row = ((int) y) / rowH - 2;
			
			int index = row * 7 + col;
			
			_cal.setFirstDayOfWeek(Calendar.MONDAY);
			_cal.set(Calendar.DAY_OF_MONTH, _cal.getActualMinimum(Calendar.DAY_OF_MONTH));
			
			int offset = _cal.get(Calendar.DAY_OF_WEEK) - 1;
			
			if (offset == 0) offset = 6; else offset = offset - 1;
			
			index -= offset;
			
			if (index < _cal.getActualMinimum(Calendar.DAY_OF_MONTH)-1) return 0;
			if (index > _cal.getActualMaximum(Calendar.DAY_OF_MONTH)-1) return 0;
			
			return index + 1;
		}
		
		@Override
		public void onDraw(Canvas canvas) {
			canvas.drawARGB(255, 255, 255, 255);
			
			int w = canvas.getWidth();
			int h = canvas.getHeight();
			
			int rowH = h / 8;
			int colW = w / 7;
			
			Paint yearMonthPaint = new Paint();
			
			yearMonthPaint.setTextAlign(Align.CENTER);
			yearMonthPaint.setTextSize(rowH);
			yearMonthPaint.setTextScaleX(((float) colW) / rowH / 3);
			yearMonthPaint.setFakeBoldText(true);
			
			String yearMonthLabel = String.format("%d %s", _cal.get(Calendar.YEAR), _cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US));
			
			canvas.drawText(yearMonthLabel, w / 2, rowH, yearMonthPaint);
			
			Paint textPaint = new Paint();
			
			textPaint.setTextAlign(Align.CENTER);
			textPaint.setTextSize(rowH);
			textPaint.setTextScaleX(((float) colW) / rowH / 3);
			
			for (int i = 0; i < 7; i++) {
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
				
				canvas.drawText(text, i * colW + colW/2, 2*rowH, textPaint);
			}

			Paint linePaint = new Paint();
			
			linePaint.setARGB(255, 0, 0, 0);

			canvas.drawLine(0, 2*rowH, w, 2*rowH, linePaint);
			
			int daysInMonth = _cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			
			_cal.setFirstDayOfWeek(Calendar.MONDAY);
			_cal.set(Calendar.DAY_OF_MONTH, _cal.getActualMinimum(Calendar.DAY_OF_MONTH));
			
			int offset = _cal.get(Calendar.DAY_OF_WEEK) - 1;
			
			if (offset == 0) offset = 6; else offset = offset - 1;
			
			for (int i = 0; i < daysInMonth; i++) {
				int col = (i + offset) % 7;
				int row = (i + offset) / 7 + 1;
				
				if ((i + 1) == _selectedDay) {
					Paint bgPaint = new Paint();
					
					bgPaint.setARGB(255, 255, 0, 0);
					bgPaint.setShader(new LinearGradient(0, 0, colW / 2, rowH / 2, Color.YELLOW, Color.YELLOW, Shader.TileMode.MIRROR));
					
					canvas.drawRect(col * colW, row * rowH + rowH, (col+1)*colW-1, row * rowH + 2*rowH-1, bgPaint);
					
					textPaint.setFakeBoldText(true);
				} else {
					textPaint.setFakeBoldText(false);
				}
				
				canvas.drawText(new Integer(i + 1).toString(), col * colW + colW/2, row * rowH + 2*rowH, textPaint);
			}
		}
		
		public void decMonth() {
			if (_cal.get(Calendar.MONTH) == _cal.getActualMinimum(Calendar.MONTH)) {
				_cal.add(Calendar.YEAR, -1);
				_cal.set(Calendar.MONTH, _cal.getActualMaximum(Calendar.MONTH));
			} else {
				_cal.add(Calendar.MONTH, -1);
			}
			
			postInvalidate();
		}

		public void incMonth() {
			if (_cal.get(Calendar.MONTH) == _cal.getActualMaximum(Calendar.MONTH)) {
				_cal.add(Calendar.YEAR, 1);
				_cal.set(Calendar.MONTH, _cal.getActualMinimum(Calendar.MONTH));
			} else {
				_cal.add(Calendar.MONTH, 1);
			}
			
			postInvalidate();
		}
		
		public interface OnDateClickedListener {
			public void handle(Date date);
		}
		
		private List<OnDateClickedListener> _onDateClickedListeners = new ArrayList<OnDateClickedListener>();
		
		public void addOnDateClickedListener(OnDateClickedListener val) {
			_onDateClickedListeners.add(val);
		}
		
		public CustomCalendarView(Context context) {
			super(context);

			Date date = new Date();
			
			_cal = new GregorianCalendar();
			
			_cal.setTime(date);
			
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
		}
	}
	
	private void startMemoList(Date date) {
		Intent intent = new Intent(this, MemoListActivity.class);
		
		intent.putExtra("date", date);
		
		startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_calendar);
		
		_rootLayout = (LinearLayout) findViewById(R.id.rootLayout);
		
		_calendarLayout = (GridLayout) findViewById(R.id.calendarLayout);
		
		_calendarView = new CustomCalendarView(getApplicationContext());
		
		_calendarView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		_calendarLayout.addView(_calendarView);
		
		_calendarView.addOnDateClickedListener(new CustomCalendarView.OnDateClickedListener() {
			@Override
			public void handle(Date date) {
				startMemoList(date);
			}
		});
		
		_decMonthBtn = (Button) findViewById(R.id.decMonthBtn);
		
		_decMonthBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_calendarView.decMonth();
			}
		});
		
		_incMonthBtn = (Button) findViewById(R.id.incMonthBtn);

		_incMonthBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				_calendarView.incMonth();
			}
		});
	}
}
