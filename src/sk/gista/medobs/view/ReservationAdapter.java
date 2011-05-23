package sk.gista.medobs.view;

import java.util.List;

import sk.gista.medobs.R;
import sk.gista.medobs.Reservation;
import sk.gista.medobs.Reservation.Status;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


public class ReservationAdapter extends ArrayAdapter<Reservation>{

	private static final int[] colors = {
		0, // empty
		Color.parseColor("#939DAC"), //disabled
		//Color.parseColor("#80B3FF"), //enabled
		Color.parseColor("#FFFFFF"), //enabled
		Color.parseColor("#71C837"), //booked
		Color.parseColor("#C83737")  //in held
	};
	private LayoutInflater inflater;
	
	public ReservationAdapter(Context context, List<Reservation> reservations) {
		super(context, R.layout.reservation_item, reservations);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Reservation reservation = getItem(position);
		View view = convertView;
		if (view == null) {
			if (getItemViewType(position) == 1) { 
				view = inflater.inflate(R.layout.reservation_booked_item, parent, false);
				TextView phoneView = (TextView) view.findViewById(R.id.patient_phone);
				TextView emailView = (TextView) view.findViewById(R.id.patient_email);
				phoneView.setText("Phone: "+reservation.getPatientPhoneNumber());
				if (reservation.getPatientEmail().length() > 0) {
					emailView.setText("Email: "+reservation.getPatientEmail());
					emailView.setVisibility(View.VISIBLE);
				} else {
					emailView.setVisibility(View.GONE);
				}
				
			} else {
				view = inflater.inflate(R.layout.reservation_item, parent, false);
			}
		}
		TextView timeText = (TextView) view.findViewById(R.id.time_text);
		TextView patientText = (TextView) view.findViewById(R.id.patient);
		timeText.setText(reservation.getTime());
		patientText.setText(reservation.getpatient());
		
		view.setBackgroundColor(colors[reservation.getStatus().numCode]);
		return view;
	}

	@Override
	public int getItemViewType(int position) {
		Status reservationStatus = getItem(position).getStatus();
		return reservationStatus == Status.booked? 1 : 0;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}
}