package com.sunshine.sample;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.sunshine.Sunshine;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		final TextView textView = (TextView) findViewById(R.id.text);

		final Sunshine sunshine = Sunshine.forView(textView)
				.build();

		sunshine.addItem(R.drawable.ic_menu_satellite__call, R.string.call, "call");
		sunshine.addItem(R.drawable.ic_menu_satellite__message, R.string.message, "message");
		sunshine.addItem(R.drawable.ic_menu_satellite__done, R.string.done, "done");

		textView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				sunshine.show();
				return false;
			}
		});

	}

}
