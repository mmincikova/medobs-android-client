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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

public class Medobs extends Activity {

	private static final String SERVER_URL_SETTING = "server_url";
	private static final String USERNAME_SETTING = "username";
	private static final String PASSWORD_SETTING = "password";
	private static final String AMBULANCE_SETTING = "ambulance";

	private static final int AMBULANCES_DIALOG = 0;
	//private static final String URL = "http://medobs.tag.sk";
	//private static final String URL = "http://10.0.2.2:8000";
	private Client client;
	private Calendar calendar;
	private List<Ambulance> ambulances;
	private Ambulance currentAmbulance;
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private SharedPreferences prefferences;
	
	private ListView reservationsView;
	private TextView selectedDateView;
	private ImageButton showCalendarButton;
	private ImageButton prevDayButton;
	private ImageButton nextDayButton;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		prefferences = PreferenceManager.getDefaultSharedPreferences(this);
		
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
		String url = prefferences.getString(SERVER_URL_SETTING, "");
		if (url.length() > 7) {
			client = new Client(url);
			String username = prefferences.getString(USERNAME_SETTING, "");
			String password = prefferences.getString(PASSWORD_SETTING, "");
			if (client.login(username, password)) {
				int lastAmbulanceId = prefferences.getInt(AMBULANCE_SETTING, 0);
				if (ambulances == null) {
					try {
						HttpResponse resp = client.httpGet("/places/");
						String content = readInputStream(resp.getEntity().getContent());
						JSONArray jsonAmbulances = new JSONArray(content);
						List<Ambulance> retrievedAmbulances = new ArrayList<Ambulance>();
						for (int i = 0; i < jsonAmbulances.length(); i++) {
							JSONObject jsonAmbulance = jsonAmbulances.getJSONObject(i);
							int id = jsonAmbulance.getInt("id");
							String name = jsonAmbulance.getString("name");
							String street = jsonAmbulance.getString("street");
							String city = jsonAmbulance.getString("city");
							Ambulance ambulance = new Ambulance(id, name, street, city);
							retrievedAmbulances.add(ambulance);
							if (id == lastAmbulanceId) {
								setAmbulance(ambulance);
							}
						}
						ambulances = retrievedAmbulances;
					} catch (IOException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				if (currentAmbulance == null && ambulances != null && ambulances.size() > 0) {
					setAmbulance(ambulances.get(0));
				}
				fetchReservations();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_select_ambulance:
			showDialog(AMBULANCES_DIALOG);
			return true;
		case R.id.menu_settings:
			saveState();
			startActivity(new Intent(this, Settings.class));
			ambulances = null;
			return true;
		}
		return false;
	}

	private void saveState() {
		if (currentAmbulance != null) {
			Editor editor = prefferences.edit();
			editor.putInt(AMBULANCE_SETTING, currentAmbulance.getId());
			editor.commit();
		}
	}

	private void fetchReservations() {
		if (client != null && currentAmbulance != null) {
			List<Reservation> reservations = new ArrayList<Reservation>();
			try {
				String date = dateFormat.format(calendar.getTime());
				HttpResponse resp = client.httpGet("/reservations/"+date+"/"+currentAmbulance.getId()+"/");
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
	}

	@Override
	protected void onPause() {
		super.onPause();
		saveState();
		if (client != null) {
			client.logout();
		}
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

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		if (id == AMBULANCES_DIALOG) {
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (which < ambulances.size()) {
						setAmbulance(ambulances.get(which));
						fetchReservations();
					}
				}
			};
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.label_select_ambulance);
			builder.setItems(new String[0], listener);
			dialog = builder.create();
		}
		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		if (id == AMBULANCES_DIALOG && ambulances != null) {
			AlertDialog layersDialog = (AlertDialog) dialog;
			
			String[] items = new String[ambulances.size()];
			int selectedItem = -1;
			for (int i = 0; i < ambulances.size(); i++) {
				items[i] = ambulances.get(i).getName();
				if (currentAmbulance != null && currentAmbulance.getId() == ambulances.get(i).getId()) {
					selectedItem = i;
				}
			}
			ListView list = layersDialog.getListView();
			list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
			
			layersDialog.getListView().setAdapter(new ArrayAdapter<String>(this, R.layout.simple_list_item_single_choice, items));
			if (selectedItem != -1) {
				list.setItemChecked(selectedItem, true);
			}
		}
	}

	private void setAmbulance(Ambulance ambulance) {
		currentAmbulance = ambulance;
		if (currentAmbulance != null) {
			setTitle(String.format("%s - %s (%s)", getString(R.string.app_name), currentAmbulance.getName(), currentAmbulance.getStreet()));
		} else {
			setTitle(getString(R.string.app_name));
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