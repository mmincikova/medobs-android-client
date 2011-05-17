package sk.gista.medobs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import sk.gista.medobs.Reservation.Status;
import sk.gista.medobs.view.ReservationAdapter;
import sk.gista.medobs.widget.DateWidget;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class Medobs extends Activity {
	
	//private static final String URL = "http://medobs.tag.sk";
	private static final String URL = "http://10.0.2.2:8000";
	private Client client;
	private Calendar calendar;
	private List<Ambulance> ambulances;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	
	private ListView reservationsView;
	private TextView selectedDateView;
	private ImageButton showCalendarButton;
	private ImageButton prevDayButton;
	private ImageButton nextDayButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		reservationsView = (ListView) findViewById(R.id.reservations_list);
		showCalendarButton = (ImageButton) findViewById(R.id.show_calendar);
		selectedDateView = (TextView) findViewById(R.id.selected_date_text);
		prevDayButton = (ImageButton) findViewById(R.id.prev_day);
		nextDayButton = (ImageButton) findViewById(R.id.next_day);
		
		calendar = Calendar.getInstance();
		
		prevDayButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				calendar.add(Calendar.DAY_OF_MONTH, -1);
				fetchReservations();
			}
		});

		nextDayButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				calendar.add(Calendar.DAY_OF_MONTH, 1);
				fetchReservations();
			}
		});

		showCalendarButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DateWidget.Open(Medobs.this, false, calendar, Calendar.MONDAY);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		client = new Client(URL);
		if (client.login("nurse", "qq")) {
			if (ambulances == null) {
				try {
					HttpResponse resp = client.httpGet("/places/");
					String content = readInputStream(resp.getEntity().getContent());
					JSONArray jsonAmbulances = new JSONArray(content);
					List<Ambulance> retrievedAmbulances = new ArrayList<Ambulance>();
					for (int i = 0; i < jsonAmbulances.length(); i++) {
						JSONObject jsonAmbulance = jsonAmbulances.getJSONObject(i);
						String name = jsonAmbulance.getString("name");
						String street = jsonAmbulance.getString("street");
						retrievedAmbulances.add(new Ambulance(name, street));
					}
					System.out.println(content);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			fetchReservations();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	private void fetchReservations() {
		List<Reservation> reservations = new ArrayList<Reservation>();
		try {
			String date = dateFormat.format(calendar.getTime());
			HttpResponse resp = client.httpGet("/reservations/"+date+"/1/");
			String content = readInputStream(resp.getEntity().getContent());
			JSONArray array = new JSONArray(content);
			for (int i = 0; i < array.length(); i++) {
				JSONObject reservation = array.getJSONObject(i);
				int status = reservation.getInt("status");
				String time = reservation.getString("time");
				String patient = reservation.getString("patient");
				reservations.add(new Reservation(time, Status.valueOf(status), patient));
			}
			selectedDateView.setText(date);
			reservationsView.setAdapter(new ReservationAdapter(this, reservations));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		client.logout();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == DateWidget.SELECT_DATE_REQUEST && data != null) {
	    	final long lDate = DateWidget.GetSelectedDateOnActivityResult(requestCode, resultCode, data.getExtras(), Calendar.getInstance());
	    	calendar.setTimeInMillis(lDate);
	    	System.out.println(calendar.get(Calendar.DAY_OF_MONTH));
			System.out.println(calendar.get(Calendar.MONTH));
	    	fetchReservations();
	  	}
	}

	public static String readInputStream(InputStream is) throws IOException {
		String line;
		StringBuilder content = new StringBuilder();
		BufferedReader input = new BufferedReader(new InputStreamReader(is), 1024);
		while ((line = input.readLine()) != null) {
			content.append(line);
		}
		input.close();
		return content.toString();
	}
}