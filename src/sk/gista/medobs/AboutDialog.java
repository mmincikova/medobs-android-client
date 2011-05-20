package sk.gista.medobs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

public class AboutDialog extends Dialog {

	public AboutDialog(Context context) {
		super(context);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);
	}

	@Override
	protected void onStart() {
		super.onStart();
		TextView versionView = (TextView) findViewById(R.id.about_version);
		versionView.setText(" version: " + Medobs.VERSION);
	}
}