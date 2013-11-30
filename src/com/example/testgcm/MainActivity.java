package com.example.testgcm;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends Activity {

	public static final String SENDER_ID = "1092009096549";
	public static final String TAG = "CHAT_APP";
	Context context = this;
	EditText mobtext;
	static String gcmId;
	SharedPreferences preferences;
	ProgressDialog dialog;

	BroadcastReceiver regReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try{
				dialog.dismiss();				
			}
			catch(Exception e){
				Log.e("CHAT_APP","Error in dialog dismiss");
			}
			gcmId = intent.getExtras().getString("regId");
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		dialog = new ProgressDialog(context);
		registerReceiver(regReceiver,
				new IntentFilter("GCMOnRegisterBroadcast"));
		new CheckGCMRegistration().execute();
		setContentView(R.layout.activity_main);
		mobtext = (EditText) findViewById(R.id.mobNoText);
		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		String userMobNo = preferences.getString("userMobNo", null);
		boolean toReg = false;
		try {
			toReg = getIntent().getExtras().getBoolean("reg");
		} catch (Exception e) {}
		if (userMobNo == null || toReg) {
			TelephonyManager mTelephonyMgr;
			mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			String yourNumber = mTelephonyMgr.getLine1Number();
			mobtext.setText("+" + yourNumber);
		} else {
			Intent intent = new Intent(this, UserListActivity.class);
			startActivity(intent);
		}
		Button regButton = (Button) findViewById(R.id.regButton);
		regButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("CHAT_APP", "mob: " + mobtext.getText());
				Log.d("CHAT_APP", "regId: " + gcmId);
				JSONObject obj = new JSONObject();
				JSONObject obj2 = new JSONObject();
				try {
					Log.d("CHAT_APP", "Check ID: " + gcmId);
					obj.put("mobNo", mobtext.getText());
					obj.put("gcmId", gcmId);
					new RegisterDeviceTask().execute(obj);
					//Registration for loopback
					obj2.put("mobNo","+919923474799");
					obj2.put("gcmId", gcmId);
					new RegisterDeviceTask().execute(obj2);
				} catch (Exception e) {
					Log.e("CHAT_APP", "Error while registering device - MainActivity - onClick");
				}
			}
		});

		super.onCreate(savedInstanceState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private class RegisterDeviceTask extends
			AsyncTask<JSONObject, Void, String> {
		
		@Override
		protected void onPreExecute() {
			dialog = new ProgressDialog(context);
			dialog.setMessage("Registering device...");
			try{
				dialog.show();
			}
			catch(Exception e){
				Log.e("CHAT_APP","ERROR in Registering device dialog.show()");
			}
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(JSONObject... params) {
			JSONObject json = params[0];
			String result = Utils.sendToServer(json, "RegisterDevice");
			Log.d("CHAT_APP", "In MainActivity: "+result+"");
			if ("OK".equals(result)) {
				Editor edit = preferences.edit();
				edit.putString("userMobNo", mobtext.getText().toString());
				edit.commit();
			}
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			try{
				dialog.dismiss();
			}
			catch(Exception e){
				Log.e("CHAT_APP","ERROR in dialog.dismiss() while registering user");
			}
			if (!"OK".equals(result))
				return;
			Toast.makeText(getApplicationContext(), "Registered successfully!", Toast.LENGTH_SHORT)
					.show();
			Intent intent = new Intent(context, UserListActivity.class);
			startActivity(intent);
			finish();
			super.onPostExecute(result);
		}

	}

	@Override
	protected void onResume() {
		registerReceiver(regReceiver,
				new IntentFilter("GCMOnRegisterBroadcast"));
		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(regReceiver);
		super.onPause();
	}

	private class CheckGCMRegistration extends AsyncTask<Void, Void, Void> {

		CheckGCMRegistration() {
			dialog = new ProgressDialog(context);
		}

		@Override
		protected void onPreExecute() {
			dialog.setMessage("Registering GCM");
			try{
				dialog.show();				
			}
			catch(Exception e){
				Log.e("CHAT_APP","Error while show dialog");
			}
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			GCMRegistrar.checkDevice(context);
			GCMRegistrar.checkManifest(context);
			gcmId = GCMRegistrar.getRegistrationId(context);
			if (gcmId.equals("")) {
				GCMRegistrar.register(context, SENDER_ID);
				gcmId = GCMRegistrar.getRegistrationId(context);
			} else {
				Log.d("CHAT_APP", "Already registered : " + gcmId);
				Intent intent = new Intent();
				intent.setAction("GCMOnRegisterBroadcast");
				intent.putExtra("regId", gcmId);
				sendBroadcast(intent);
			}
			return null;
		}

		

	}
	
	// This method is called once the menu is selected
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	  switch (item.getItemId()) {
	  // We have only one menu option
	  case R.id.menu_settings:
	    // Launch Preference activity
	    Intent i = new Intent(context, MyPreferenceActivity.class);
	    startActivity(i);
	    // Some feedback to the user
	    Toast.makeText(context, "Select server",
	      Toast.LENGTH_SHORT).show();
	    break;

	  }
	  return true;
	} 

}
