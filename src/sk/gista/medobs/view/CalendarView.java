package sk.gista.medobs.view;

import java.util.*;
import android.content.Context;
import android.widget.*;
import android.util.AttributeSet;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import java.text.SimpleDateFormat;


public class CalendarView extends LinearLayout {
	// fields
	private static String sStrSelect = "Select day";
	private static String sStrSelected = "Selected day:";
	private static String sStrNone = "none";

	// fields
	private ArrayList<DateWidgetDayCell> days = new ArrayList<DateWidgetDayCell>();

	// fields
	private SimpleDateFormat dateMonth = new SimpleDateFormat("MMMM yyyy");
	private SimpleDateFormat dateFull = new SimpleDateFormat("d MMMM yyyy");

	// fields
	private Calendar calStartDate = Calendar.getInstance();
	private Calendar calToday = Calendar.getInstance();
	private Calendar calCalendar = Calendar.getInstance();
	private Calendar calSelected = Calendar.getInstance();
	private Calendar currentMonth = Calendar.getInstance();

	private CalendarListener listener;
	private List<Integer> enabledDays;

	// fields
	LinearLayout layContent = null;
	Button btnPrev = null;
	Button btnToday = null;
	Button btnNext = null;
	Button btnNone = null;

	// fields
	private boolean bNoneButton = true;
	private int iFirstDayOfWeek = Calendar.MONDAY;

	// fields
	private int iMonthViewCurrentMonth = 0;
	private int iMonthViewCurrentYear = 0;

	// fields
	public static final int SELECT_DATE_REQUEST = 111;
	private int iDayCellSize = 39;
	private int iDayHeaderHeight = 24;
	private int iTotalWidth = (iDayCellSize * 7);
	private int iSmallButtonWidth = 100;

	private float density;

	public CalendarView(Context context) {
		super(context);
		init(Calendar.getInstance(), Calendar.MONDAY, false);
	}

	public CalendarView(Context context, AttributeSet attribs) {
		super(context, attribs);
		init(Calendar.getInstance(), Calendar.MONDAY, false);
	}

	public CalendarView(Context context, Calendar calendar, int firstDayOfWeek, boolean noneButton) {
		super(context);
		init(calendar, firstDayOfWeek, noneButton);
	}

	// methods
	private final void init(Calendar calendar, int firstDayOfWeek, boolean noneButton) {
		density = getResources().getDisplayMetrics().density;
		iDayCellSize *= density;
		iDayHeaderHeight *= density;
		iTotalWidth *= density;
		iSmallButtonWidth *= density;
		
		// init calendar to defaults
		calSelected = calendar;
		iFirstDayOfWeek = firstDayOfWeek;
		bNoneButton = noneButton;
		generateContentView();

		// initialize
		calStartDate = getCalendarStartDate();
		DateWidgetDayCell daySelected = updateCalendar();
		updateControlsState();

		// none button
		if (bNoneButton) {
			btnNone.requestFocus();
			btnNone.setEnabled(true);
			btnNone.setFocusable(true);
		} else {
			btnNone.setEnabled(false);
			btnNone.setFocusable(false);
		}

		// focus selected day
		if (daySelected != null)
			daySelected.requestFocus();
	}

	public static void setStrings(String strSelect, String strSelected, String strNone) {
		sStrSelect = new String(strSelect);
		sStrSelected = new String(strSelected);
		sStrNone = new String(strNone);
	}

	public LinearLayout createLayout(int iOrientation) {
		LinearLayout lay = new LinearLayout(getContext());
		lay.setLayoutParams(new LayoutParams(android.view.ViewGroup.LayoutParams.FILL_PARENT,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		lay.setOrientation(iOrientation);
		return lay;
	}

	public Button createButton(String sText, int iWidth, int iHeight) {
		Button btn = new Button(getContext());
		btn.setText(sText);
		btn.setLayoutParams(new LayoutParams(iWidth, iHeight));
		return btn;
	}

	public TextView createLabel(String sText, int iWidth, int iHeight) {
		TextView label = new TextView(getContext());
		label.setText(sText);
		label.setLayoutParams(new LayoutParams(iWidth, iHeight));
		return label;
	}

	private void generateTopButtons(LinearLayout layTopControls) {
		final int iHorPadding = (int) (24 * density);
		final int iSmallButtonWidth = (int) (60 * density);

		// create buttons
		btnToday = createButton("", iTotalWidth - iSmallButtonWidth - iSmallButtonWidth,
				android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		btnToday.setPadding(iHorPadding, btnToday.getPaddingTop(), iHorPadding, btnToday.getPaddingBottom());
		btnToday.setBackgroundResource(android.R.drawable.btn_default_small);

		btnPrev = new SymbolButton(getContext(), SymbolButton.symbol.arrowLeft);
		btnPrev.setLayoutParams(new LayoutParams(iSmallButtonWidth, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		btnPrev.setBackgroundResource(android.R.drawable.btn_default_small);

		btnNext = new SymbolButton(getContext(), SymbolButton.symbol.arrowRight);
		btnNext.setLayoutParams(new LayoutParams(iSmallButtonWidth, android.view.ViewGroup.LayoutParams.WRAP_CONTENT));
		btnNext.setBackgroundResource(android.R.drawable.btn_default_small);

		// set events
		btnPrev.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				enabledDays = null;
				setPrevViewItem();
				if (listener != null) {
					listener.onMonthChanged(CalendarView.this, currentMonth); // or iMonthViewCurrentMonth ?
				}
			}
		});
		btnToday.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				enabledDays = null;
				setTodayViewItem();
				if (listener != null) {
					listener.onMonthChanged(CalendarView.this, currentMonth);
				}
			}
		});
		btnNext.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				enabledDays = null;
				setNextViewItem();
				if (listener != null) {
					listener.onMonthChanged(CalendarView.this, currentMonth);
				}
			}
		});

		layTopControls.setGravity(Gravity.CENTER_HORIZONTAL);
		layTopControls.addView(btnPrev);
		layTopControls.addView(btnToday);
		layTopControls.addView(btnNext);
	}

	public void generateBottomButtons(LinearLayout layBottomControls) {
		btnNone = createButton(sStrNone, iSmallButtonWidth, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
		btnNone.setBackgroundResource(android.R.drawable.btn_default_small);

		// set events
		btnNone.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View arg0) {
				deselectAll();
				updateControlsState();
				//OnClose(); //TODO
			}
		});

		layBottomControls.setGravity(Gravity.CENTER_HORIZONTAL);
		layBottomControls.addView(btnNone);
	}

	private void generateContentView() {
		setOrientation(LinearLayout.VERTICAL);
		
		LinearLayout layTopControls = createLayout(LinearLayout.HORIZONTAL);
		LinearLayout layContentTop = createLayout(LinearLayout.HORIZONTAL);
		LinearLayout layContentBottom = createLayout(LinearLayout.HORIZONTAL);
		LinearLayout layBottomControls = createLayout(LinearLayout.HORIZONTAL);

		layContent = createLayout(LinearLayout.VERTICAL);
		//layContent.setPadding(8, 0, 8, 0);

		generateTopButtons(layTopControls);
		generateBottomButtons(layBottomControls);

		layContentTop.getLayoutParams().height = (int) (12 * density);
		layContentBottom.getLayoutParams().height = (int) (18 * density);

		generateCalendar(layContent);

		if (!bNoneButton)
			layBottomControls.getLayoutParams().height = 0;

		addView(layTopControls);
		addView(layContentTop);
		addView(layContent);
		addView(layContentBottom);
		addView(layBottomControls);
	}

	private View generateCalendarRow() {
		LinearLayout layRow = createLayout(LinearLayout.HORIZONTAL);
		for (int iDay = 0; iDay < 7; iDay++) {
			DateWidgetDayCell dayCell = new DateWidgetDayCell(getContext(), iDayCellSize, iDayCellSize);
			dayCell.setItemClick(mOnDayCellClick);
			days.add(dayCell);
			layRow.addView(dayCell);
		}
		return layRow;
	}

	private View generateCalendarHeader() {
		LinearLayout layRow = createLayout(LinearLayout.HORIZONTAL);
		for (int iDay = 0; iDay < 7; iDay++) {
			DateWidgetDayHeader day = new DateWidgetDayHeader(getContext(), iDayCellSize, iDayHeaderHeight);
			final int iWeekDay = DayStyle.getWeekDay(iDay, iFirstDayOfWeek);
			day.setData(iWeekDay);
			layRow.addView(day);
		}
		return layRow;
	}

	private void generateCalendar(LinearLayout layContent) {
		// generate days header
		layContent.addView(generateCalendarHeader());
		// generate days
		days.clear();
		for (int iRow = 0; iRow < 6; iRow++) {
			layContent.addView(generateCalendarRow());
		}
	}

	private Calendar getCalendarStartDate() {
		calToday.setTimeInMillis(System.currentTimeMillis());
		calToday.setFirstDayOfWeek(iFirstDayOfWeek);

		if (calSelected.getTimeInMillis() == 0) {
			calStartDate.setTimeInMillis(System.currentTimeMillis());
			calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
		} else {
			calStartDate.setTimeInMillis(calSelected.getTimeInMillis());
			calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);
		}

		UpdateStartDateForMonth();

		return calStartDate;
	}

	private DateWidgetDayCell updateCalendar() {
		DateWidgetDayCell daySelected = null;
		boolean bSelected = false;

		final boolean bIsSelection = (calSelected.getTimeInMillis() != 0);
		final int iSelectedYear = calSelected.get(Calendar.YEAR);
		final int iSelectedMonth = calSelected.get(Calendar.MONTH);
		final int iSelectedDay = calSelected.get(Calendar.DAY_OF_MONTH);

		calCalendar.setTimeInMillis(calStartDate.getTimeInMillis());

		for (int i = 0; i < days.size(); i++) {
			final int iYear = calCalendar.get(Calendar.YEAR);
			final int iMonth = calCalendar.get(Calendar.MONTH);
			final int iDay = calCalendar.get(Calendar.DAY_OF_MONTH);
			final int iDayOfWeek = calCalendar.get(Calendar.DAY_OF_WEEK);

			DateWidgetDayCell dayCell = days.get(i);

			// check today
			boolean bToday = false;
			if (calToday.get(Calendar.YEAR) == iYear)
				if (calToday.get(Calendar.MONTH) == iMonth)
					if (calToday.get(Calendar.DAY_OF_MONTH) == iDay)
						bToday = true;

			// check holiday
			boolean bHoliday = false;
			if ((iDayOfWeek == Calendar.SATURDAY) || (iDayOfWeek == Calendar.SUNDAY))
				bHoliday = true;
			if ((iMonth == Calendar.JANUARY) && (iDay == 1))
				bHoliday = true;
			boolean enabled = (enabledDays != null && (iMonth == iMonthViewCurrentMonth && enabledDays.contains(iDay)));
			dayCell.setData(iYear, iMonth, iDay, bToday, bHoliday, iMonthViewCurrentMonth, enabled);

			// check if selected day
			bSelected = false;
			if (bIsSelection)
				if ((iSelectedDay == iDay) && (iSelectedMonth == iMonth) && (iSelectedYear == iYear))
					bSelected = true;

			dayCell.setSelected(bSelected);
			if (bSelected)
				daySelected = dayCell;

			calCalendar.add(Calendar.DAY_OF_MONTH, 1);
		}

		layContent.invalidate();
		return daySelected;
	}

	private void UpdateStartDateForMonth() {
		iMonthViewCurrentMonth = calStartDate.get(Calendar.MONTH);
		iMonthViewCurrentYear = calStartDate.get(Calendar.YEAR);
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);

		UpdateCurrentMonthDisplay();

		// update days for week
		int iDay = 0;
		int iStartDay = iFirstDayOfWeek;
		if (iStartDay == Calendar.MONDAY) {
			iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY;
			if (iDay < 0)
				iDay = 6;
		}
		if (iStartDay == Calendar.SUNDAY) {
			iDay = calStartDate.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY;
			if (iDay < 0)
				iDay = 6;
		}
		calStartDate.add(Calendar.DAY_OF_WEEK, -iDay);
	}

	private void UpdateCurrentMonthDisplay() {
		currentMonth.setTimeInMillis(calStartDate.getTimeInMillis());
		String s = dateMonth.format(calStartDate.getTime());
		btnToday.setText(s);
	}

	public void setPrevViewItem() {
		iMonthViewCurrentMonth--;
		if (iMonthViewCurrentMonth == -1) {
			iMonthViewCurrentMonth = 11;
			iMonthViewCurrentYear--;
		}
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
		calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
		UpdateStartDateForMonth();
		updateCalendar();
	}

	public void setTodayViewItem() {
		calToday.setTimeInMillis(System.currentTimeMillis());
		calToday.setFirstDayOfWeek(iFirstDayOfWeek);

		calStartDate.setTimeInMillis(calToday.getTimeInMillis());
		calStartDate.setFirstDayOfWeek(iFirstDayOfWeek);

		UpdateStartDateForMonth();
		updateCalendar();
	}

	public void setNextViewItem() {
		iMonthViewCurrentMonth++;
		if (iMonthViewCurrentMonth == 12) {
			iMonthViewCurrentMonth = 0;
			iMonthViewCurrentYear++;
		}
		calStartDate.set(Calendar.DAY_OF_MONTH, 1);
		calStartDate.set(Calendar.MONTH, iMonthViewCurrentMonth);
		calStartDate.set(Calendar.YEAR, iMonthViewCurrentYear);
		UpdateStartDateForMonth();
		updateCalendar();
	}

	public DateWidgetDayCell.OnItemClick mOnDayCellClick = new DateWidgetDayCell.OnItemClick() {
		public void OnClick(DateWidgetDayCell item) {
			deselectAll();
			calSelected.setTimeInMillis(item.getDate().getTimeInMillis());
			item.setSelected(true);
			updateControlsState();
			if (listener != null) {
				listener.onDateSelected(CalendarView.this);
			}
		}
	};

	public void updateControlsState() {
		final boolean bDaySelected = (calSelected.getTimeInMillis() != 0);
		btnNone.setEnabled(bDaySelected);
		if (bDaySelected) {
			//String s = dateFull.format(calSelected.getTime());
			//setTitle(sStrSelected + " " + s); // TODO
		} else {
			//setTitle(sStrSelect); // TODO
		}
	}

	public void deselectAll() {
		calSelected.setTimeInMillis(0);
		for (int i = 0; i < days.size(); i++) {
			DateWidgetDayCell dayCell = days.get(i);
			if (dayCell.getSelected())
				dayCell.setSelected(false);
		}
		layContent.invalidate();
	}

	public void setCalendarListener(CalendarListener listener) {
		this.listener = listener;
	}

	public Calendar getSelectedValue() {
		return calSelected;
	}

	public void setEnabledDays(List<Integer> enabledDays) {
		this.enabledDays = enabledDays;
		updateCalendar();
	}

	public void setOnPrevMonthListener(View.OnClickListener listener) {
		btnPrev.setOnClickListener(listener);
	}

	public void setOnNextMonthListener(View.OnClickListener listener) {
		btnNext.setOnClickListener(listener);
	}

	public Calendar getCurrentMonth() {
		return currentMonth;
	}

	public void setSelectedDate(Calendar calendar) {
		calSelected = calendar;
		calStartDate = getCalendarStartDate();
		updateCalendar();
	}
}
