package com.sunshine.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sunshine.SatelliteMenu;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final TextView textView = (TextView) findViewById(R.id.text);

		final SatelliteMenu satelliteMenu = new SatelliteMenu.Builder(textView).build();

		satelliteMenu.setWindowCovered(true);

		satelliteMenu.addItem(R.drawable.ic_menu_satellite__call, R.string.call, "call");

		textView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				satelliteMenu.show();
				return false;
			}
		});

	}

}
