package com.example.mindtray;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import com.example.mindtray.memo.Memo;
import com.example.mindtray.memolist.MemoListActivity;
import com.example.mindtray.shared.Storage;
import com.example.mindtray.shared.Util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CalendarActivity extends Activity {
	private FrameLayout _layout_root;
	private LinearLayout _calendarLayout;
	private LinearLayout _calendarHeaderLayout;
	private LinearLayout _calendarBodyLayout;

	private CustomCalendarView _calendarView;
	
	private Button _button_decMonth;
	private Button _button_incMonth;
	
	private TextView _textView_title;
	
	private Button _button_back;

	//custom painted calendar
	private static class CustomCalendarView extends View {
		private Calendar _cal;
		private int _selectedDay = 0;
		
		private final int _rowC = 6;
		private final int _colC = 7;
		
		private int getDayAtCoords(float x, float y) {
			int w = getWidth();
			int h = getHeight();

			if (x < 0 || x >= w || y < 0 || y >= h) return 0;

			int rowH = h / _rowC;
			int colW = w / _colC;
			
			if (y < rowH) {				
				return 0;
			}

			int col = ((int) x) / colW;
			int row = ((int) y) / rowH - 1;

			if (col >= _colC || row >= _rowC) return 0;

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

				_dateTextPaint.setTextSize(_rowH);
				_dateTextPaint.setTextScaleX(((float) _colW) / _rowH / 3);
				
				_linePaint.setARGB(255, 0, 0, 0);
				
				_selectedShader = new LinearGradient(0, 0, _colW / 2, _rowH / 2, Color.YELLOW, Color.YELLOW, Shader.TileMode.MIRROR);
				_memoDatesShader = new LinearGradient(0, 0, _colW / 2, _rowH / 2, Color.YELLOW, Color.RED, Shader.TileMode.MIRROR);
				
				_selectedBGPaint.setARGB(127, 255, 0, 0);
				_selectedBGPaint.setShader(_selectedShader);
				_selectedBGPaint.setStrokeWidth(10);
				_selectedBGPaint.setStyle(Paint.Style.STROKE);
				
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
				_memoTextPaint.setColor(Color.BLACK);
				_memoTextPaint.setFakeBoldText(true);
			} 
		}
		
		@Override
		public void onSizeChanged(int w, int h, int oldw, int oldh) {
			
		}
		
		@Override
		public void onDraw(Canvas canvas) {
			//canvas.drawColor(_bgColorVal.data);
			
			int w = canvas.getClipBounds().width();
			int h = canvas.getClipBounds().height();

			updateSize(w, h);

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
				
				_paintCal.set(_paintCal.get(Calendar.YEAR), _paintCal.get(Calendar.MONTH), i + 1, 0, 0, 0);

				Calendar curDate = _paintCal;
				Paint curDateTextPaint = _dateTextPaint;
				
				String text = Integer.toString(i + 1);
				
				if ((i + 1) == _selectedDay) {
					RectF rect = new RectF(col * _colW, row * _rowH, (col + 1) * _colW - 1, row * _rowH + _rowH - 1);

					canvas.drawRoundRect(rect, rect.width() * 0.5F, rect.height() * 0.5F, _selectedBGPaint);
					
					curDateTextPaint = _selectedTextPaint;
				} else {
					for (int j = 0; j < _memoDates.size(); j++) {
						Calendar date = _memoDates.get(j);

						if (date.get(Calendar.YEAR) == curDate.get(Calendar.YEAR) && date.get(Calendar.MONTH) == curDate.get(Calendar.MONTH) && date.get(Calendar.DAY_OF_MONTH) == curDate.get(Calendar.DAY_OF_MONTH)) {
							_bgPaint.setARGB(127, 255, 0, 0);
							_bgPaint.setShader(_memoDatesShader);
							
							//canvas.drawRect(col * _colW, row * _rowH, (col + 1) * _colW - 1, row * _rowH + _rowH - 1, _bgPaint);
							RectF rect = new RectF(col * _colW, row * _rowH, (col + 1) * _colW - 1, row * _rowH + _rowH - 1);

							canvas.drawRoundRect(rect, rect.width() * 0.5F, rect.height() * 0.5F, _bgPaint);
							
							curDateTextPaint = _memoTextPaint;

							break;
						}
					}
				}
				
				curDateTextPaint.getTextBounds(text, 0, text.length(), _textBounds);
				canvas.drawText(text, col * _colW + _colW / 2 - _textBounds.exactCenterX(), row * _rowH + _rowH / 2 - _textBounds.exactCenterY(), curDateTextPaint);
			}
		}
		
		private TextView _textView_title;
		
		private void updateTitle() {
			String yearMonthLabel = String.format(Locale.US, "%d %s", _cal.get(Calendar.YEAR), _cal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US));

			_textView_title.setText(yearMonthLabel);
			postInvalidate();
		}
		
		private void setTitle(TextView val) {
			_textView_title = val;
			
			updateTitle();
		}
		
		public void decMonth() {
			//noinspection WrongConstant
			if (_cal.get(Calendar.MONTH) == _cal.getActualMinimum(Calendar.MONTH)) {
				_cal.add(Calendar.YEAR, -1);
				_cal.set(Calendar.MONTH, _cal.getActualMaximum(Calendar.MONTH));
			} else {
				_cal.add(Calendar.MONTH, -1);
			}

			updateTitle();
		}

		public void incMonth() {
			//noinspection WrongConstant
			if (_cal.get(Calendar.MONTH) == _cal.getActualMaximum(Calendar.MONTH)) {
				_cal.add(Calendar.YEAR, 1);
				_cal.set(Calendar.MONTH, _cal.getActualMinimum(Calendar.MONTH));
			} else {
				_cal.add(Calendar.MONTH, 1);
			}

			updateTitle();
		}
		
		public interface OnDateClickedListener {
			public void handle(Calendar date);
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
		
		private List<Calendar> _memoDates = new ArrayList<Calendar>();

		private void loadMemos() {
			List<Memo> storageMemos = Storage.getInstance(getContext()).getMemos();

			_memoDates.clear();

			for (Memo memo : storageMemos) {
				_memoDates.add(memo.getDate());
			}

			postInvalidate();
		}

		public CustomCalendarView(Context context) {
			super(context);
	
			_bgColorVal = new TypedValue();
			
			getContext().getTheme().resolveAttribute(android.R.attr.windowBackground, _bgColorVal, true);
			
			Date date = new Date();
			
			_cal = new GregorianCalendar();
			
			_cal.setTime(date);
			
			_dateTextPaint.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/AlexBrush-Regular-OTF.otf"));
			
			postInvalidate();
			
			setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {					
					int day = getDayAtCoords(event.getX(), event.getY());

					if (event.getAction() == MotionEvent.ACTION_UP) {
						_selectedDay = 0;
					} else {
						_selectedDay = day;
					}
					
					postInvalidate();
					
					if (day == 0) return true;
					if (event.getAction() != MotionEvent.ACTION_UP) return true;
					
					v.performClick();
					
					_cal.set(Calendar.DAY_OF_MONTH, day);
					
					Calendar date = _cal;
					
					for (OnDateClickedListener listener : _onDateClickedListeners) {
						listener.handle(date);
					}
					
					return true;
				}
			});

			loadMemos();
		}
	}

	private void startMemoList(Calendar date) {
		Util.lockViews(_layout_root, false);

		Intent intent = new Intent(this, MemoListActivity.class);

		intent.setFlags(intent.getFlags() | Intent.FLAG_ACTIVITY_NO_HISTORY);
		intent.putExtra("date", date);
		
		startActivityForResult(intent, 0);
	}
	
	public void button_back_onClick(View view) {
		finish();
	}

	private void loadMemos() {

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_calendar);
		
		_layout_root = (FrameLayout) findViewById(R.id.layout_root);
		
		_calendarLayout = (LinearLayout) findViewById(R.id.layout_calendar);
		
		_calendarHeaderLayout = (LinearLayout) findViewById(R.id.layout_calendar_header);
		_calendarBodyLayout = (LinearLayout) findViewById(R.id.layout_calendar_body);
		
		_calendarView = new CustomCalendarView(getApplicationContext());
		
		//_calendarView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		
		_calendarBodyLayout.addView(_calendarView);
		
		_calendarView.addOnDateClickedListener(new CustomCalendarView.OnDateClickedListener() {
			@Override
			public void handle(Calendar date) {
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

		loadMemos();

		Util.animateBackground(this, (ImageView) findViewById(R.id.bg1));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		Util.lockViews(_layout_root, true);
	}
}
