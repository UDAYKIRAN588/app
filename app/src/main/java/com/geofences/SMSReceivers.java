package com.geofences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;

public class SMSReceivers extends Activity {

	ImageButton add;
	ListView listView;

	SharedPreferences shared;
	Editor edit;
	static final int PICK_CONTACT=1;

	ArrayList<String> nos = new ArrayList<String>();
	ArrayList<String> namesArray = new ArrayList<String>();

	String[] phonenos;
	int i;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		shared = getApplicationContext().getSharedPreferences("GeofencesReceivers", MODE_MULTI_PROCESS);
		edit = shared.edit();

		setContentView(R.layout.activity_smsreceivers);
		listView = (ListView) findViewById(R.id.sms_receivers);
		add = (ImageButton) findViewById(R.id.add_receiver);

		retreiveList();
		
		add.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intent, PICK_CONTACT);
			}
		});

		//delete contact from list
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView arg0, View arg1, int arg2, long arg3) {
				// TODO Auto-generated method stub
				String key = namesArray.get(arg2).toString();
				alertDelete(key);
				return false;
			}
		});
	}

	//method to delete contact with alert
	public void alertDelete(final String name)
	{
		Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

				dialog.dismiss();
				edit.remove(name).commit();
				retreiveList();
			}

		});
		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();;
			}

		});

		builder.show();
	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (PICK_CONTACT) :
			if (resultCode == Activity.RESULT_OK) {

				Uri contactData = data.getData();
				Cursor c =  managedQuery(contactData, null, null, null, null);
				if (c.moveToFirst()) {

					String id =c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));

					String hasPhone =c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
					i = 0;
					if (hasPhone.equalsIgnoreCase("1")) {
						Cursor phones = getContentResolver().query( 
								ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null, 
								ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id, 
								null, null);
						phones.moveToFirst();
						phonenos = new String[phones.getCount()];
						do{
							String  cNumber = phones.getString(phones.getColumnIndex("data1"));
							phonenos[i] = cNumber;
							System.out.println("number is:"+cNumber);
							i++;
						}while(phones.moveToNext());
					}
					String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
					selectPhoneNumber(name);
					System.out.println("name is:"+name);
				}
			}
		break;
		}
	}

	public void retreiveList()
	{
		nos.clear();
		namesArray.clear();
		listView.setAdapter(null);
		try{
			Map<String,?> keys = shared.getAll();

			for(Map.Entry<String,?> entry : keys.entrySet()){
				nos.add(entry.getKey()+"\n"+entry.getValue().toString());
				namesArray.add(entry.getKey());
			}
		}
		catch(Exception e){}
		//setup the adapter
		listView.setAdapter(new ArrayAdapter<>(getApplicationContext(), R.layout.list_layout,R.id.list_content,nos));

	}
	//select phone number
	public void selectPhoneNumber(final String name)
	{
		final Builder builder= new AlertDialog.Builder(this);
		builder.setTitle("Set Number for "+name.toUpperCase());
		builder.setCancelable(false);
		builder.setItems(phonenos,  new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				edit.putString(name, phonenos[which]).commit();
				retreiveList();
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		builder.show();
	}
}
