package com.geofences;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class NotificationService extends Service{

	SharedPreferences shared;
	Editor edit;
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		shared = getApplication().getSharedPreferences("Geofences", MODE_WORLD_WRITEABLE);
		edit = shared.edit();
		edit.remove("latitude").commit();
		edit.remove("longitude").commit();
		generateNotif();
		return Service.START_STICKY;
	}

	//generate a ongoing notification
	public void generateNotif()
	{
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentTitle("Geofences");
		builder.setContentText("Tap to reset Current location and start geofences");
		builder.setOngoing(true);

		Intent notificationIntent = new Intent(this, MainActivity.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this,
				0, notificationIntent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		builder.setContentIntent(contentIntent);


		NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(1001, builder.build());
	}
	
}

