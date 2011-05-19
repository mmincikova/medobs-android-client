package sk.gista.medobs.view;

import java.util.Calendar;


public interface CalendarListener {

	void onMonthChanged(CalendarView view, Calendar value);
	void onDateSelected(CalendarView view);
}
