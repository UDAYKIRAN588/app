package com.geofences;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

	ListView list_view;
	Button address;
	String[] list;

	SharedPreferences shared;
	Editor edit;

	String lati,longi;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		shared = getApplicationContext().getSharedPreferences("Geofences", MODE_MULTI_PROCESS);
		edit = shared.edit();

		//retreive settings from shared preferences
		retrieveValues();

		list = getResources().getStringArray(R.array.mainList);
		setContentView(R.layout.activity_main);
		list_view = (ListView) findViewById(R.id.main_list);
		address = (Button) findViewById(R.id.get_location);

		//setting adapter
		list_view.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.list_layout,R.id.list_content, list));

		if (lati.equals("")) {
			geoCalci(true);
		}
		else{
			geoCalci(false);
		}

		list_view.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				if (position == 0) {
					geoCalci(true);
				}
				if (position == 1) {
					startActivity(new Intent(MainActivity.this, SMSReceivers.class));
				}
				if (position == 2) {
					setDistanceLimitDialog(MainActivity.this);
				}
				if (position == 3) {
					try{
						stopService(new Intent(MainActivity.this, GeoFenceDetector.class));
						Toast.makeText(getApplicationContext(), "Service has been stopped", 400).show();
					}
					catch(Exception e){}
				}
			}
		});
	}
	private class GeocoderHandler extends Handler {
		@Override
		public void handleMessage(Message message) {
			String locationAddress;
			switch (message.what) {
			case 1:
				Bundle bundle = message.getData();
				locationAddress = bundle.getString("address");
				break;
			default:
				locationAddress = null;
			}
			address.setText(""+locationAddress);
		}
	}

	public void retrieveValues()
	{
		try{
			lati = shared.getString("latitude", "");
			longi = shared.getString("longitude", "");
		}
		catch(Exception e){}
	}
	public void geoCalci(boolean store)
	{
		GeofencesService gps = new GeofencesService(MainActivity.this);

		double latitude,longitude;
		// check if GPS enabled     
		if(gps.canGetLocation()){

			if (store) {
				latitude = gps.getLatitude();
				longitude = gps.getLongitude();

				edit.putString("latitude", ""+latitude).commit();
				edit.putString("longitude", ""+longitude).commit();
			}
			else{
				latitude = Double.parseDouble(lati);
				longitude = Double.parseDouble(longi);
			}

			LocationAddress locationAddress = new LocationAddress();
			locationAddress.getAddressFromLocation(latitude, longitude,getApplicationContext(), new GeocoderHandler());

			try{
				startService(new Intent(MainActivity.this, GeoFenceDetector.class));
			}
			catch(Exception e){}
		}else{
			// can't get location
			// GPS or Network is not enabled
			// Ask user to enable GPS/network in settings
			gps.showSettingsAlert();
		}
	}

	public void setDistanceLimitDialog(Context context)
	{
		// get prompts.xml view
		LayoutInflater li = LayoutInflater.from(context);
		View promptsView = li.inflate(R.layout.prompts, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

		// set prompts.xml to alertdialog builder
		alertDialogBuilder.setView(promptsView);

		final EditText userInput = (EditText) promptsView
				.findViewById(R.id.editTextDialogUserInput);

		// set dialog message
		alertDialogBuilder.setCancelable(false).setPositiveButton("OK",new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog,int id) {
				// get user input and set it to result
				// edit text
				String dist = userInput.getText().toString();
				edit.putString("distance", dist).commit();
			}
		})
		.setNegativeButton("Cancel",new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog,int id) {
				dialog.cancel();
			}
		});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();

		// show it
		alertDialog.show();

	}
}
