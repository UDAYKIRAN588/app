package com.geofences;

import java.util.ArrayList;
import java.util.Map;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.widget.ArrayAdapter;
import android.widget.Toast;

//service to message stored numbers as soon as distance exceeds
public class GeoFenceDetector extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	//shared preferences to store settings and phone numbers
	SharedPreferences shared,shared1;
	
	//arraylist to store numbers retrieved from shared prefernces
	ArrayList<String> nos = new ArrayList<String>();

	//thread 
	Thread mThread;

	//global variables to store values 
	String lati,longi,dist;
	double latitude,longitude,distance;

	//sticky service to rubn in background
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		//retrieving shared preferences from internal memory
		shared1 = getApplicationContext().getSharedPreferences("GeofencesReceivers", MODE_MULTI_PROCESS);
		shared = getApplicationContext().getSharedPreferences("Geofences", MODE_MULTI_PROCESS);
		
		//method to retrieve stored phone numbers
		retreiveList();
		
		//retreiving settings
		try{
			lati = shared.getString("latitude", "");
			longi = shared.getString("longitude", "");
			dist = shared.getString("distance", "100");
		}
		catch(Exception e){}

		//conversion of string to double values
		latitude = Double.parseDouble(lati);
		longitude = Double.parseDouble(longi);
		distance = Double.parseDouble(dist);

		//start thread to calculate distance
		mThread = new MonitorThread(this);
		mThread.start();
		return Service.START_STICKY;
	}

	//thread to get location and calculate diatnce
	private class MonitorThread extends Thread{

		Context mcnt;
		public MonitorThread(Context cnt){
			//debug: log.i("Detector","Monitor//debug: logThread");
			mcnt = cnt;
		}

		@Override
		public void run() {
			while(!this.isInterrupted() ){

				try {
					Thread.sleep(1000);
					
					//service class used to detect GPS  
					GeofencesService gps = new GeofencesService(GeoFenceDetector.this);

					// check if GPS enabled     
					if(gps.canGetLocation()){
						
						//getting current location details
						LocationManager locationManager = (LocationManager) mcnt.getSystemService(LOCATION_SERVICE);
						Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
						
						if (location != null) {
							//current latitude and lobngitude details
							double latitude1 = location.getLatitude();
							double longitude1 = location.getLongitude();
							
							//calculate distance
							double distanceCalci = DistanceCalculator.distance(latitude, longitude, latitude1, longitude1, "K");
					
							//comparing stored distance to calculated distance
							if (distanceCalci > distance && latitude != 0) {
								//call method to send messages
								sendMessages();
								//stop service as soon as messages are sent
								stopService(new Intent(GeoFenceDetector.this, GeoFenceDetector.class));
								stopService(new Intent(GeoFenceDetector.this, NotificationService.class));
							}
						}
					}else{
						// can't get location
						// GPS or Network is not enabled
						// Ask user to enable GPS/network in settings
						gps.showSettingsAlert();
					}
				} catch (InterruptedException e) {
					// good practice
					Thread.currentThread().interrupt();
					return;
				}
			}
		}
	}  
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		if (mThread != null) {
			mThread.interrupt();	
		}

		super.onDestroy();
	}

	//retrieve list of phone numbers method
	public void retreiveList()
	{
		nos.clear();
		try{
			Map<String,?> keys = shared1.getAll();

			for(Map.Entry<String,?> entry : keys.entrySet()){
				nos.add(entry.getValue().toString());
			}
		}
		catch(Exception e){}

	}

	//method to send messages to stored phone numbers
	public void sendMessages()
	{
		for (int i = 0; i < nos.size(); i++) {
			try {
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(nos.get(i), null, "Your friend/family member has travelled more than "+distance+" KM", null, null);

			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		try{
			stopService(new Intent(GeoFenceDetector.this, GeoFenceDetector.class));
		}
		catch(Exception e){}
		
	}
}
