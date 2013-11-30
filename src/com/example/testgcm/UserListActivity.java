package com.example.testgcm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.chatatainment.database.MessageDataSource;
import com.chatatainment.database.UsersDataSource;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

public class UserListActivity extends ListActivity {

	private UsersDataSource datasource;
	private UsersDataSource datasourceForRead;
	private UserListingAdapter adapter;
	private List<User> usersToDisplay;
	private ListView listView;
	private String myNumber;
	Context context;

	public UserListActivity() {
		context = this;
	}
	
	private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("CHAT_APP", "BroadCast Received");
			adapter.notifyDataSetChanged();			
		}
	};
	private BroadcastReceiver messageReceived = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			MessageDataSource db = new MessageDataSource(getApplicationContext());
			db.openForRead();
			HashMap<String,Integer> unreadUsers = db.getUnviewedMessageMap(myNumber);
			
			for (Map.Entry<String, Integer> entry : unreadUsers.entrySet()) {
			    String key = entry.getKey();
			    Object value = entry.getValue();
			    Log.d("UNREAD",key+" : "+value);
			}
			datasource.openForRead();
			
			List<User> u = datasource.getRegisteredUsers();
			Log.d("UNREAD","In UserListActivity: unread users: "+u.size());
			for(User each: u){
				if(unreadUsers.containsKey(each)){
					each.setRead(false);
				}
			}
			//updateRegisteredUsers();
			//adapter = new UserListingAdapter(getApplicationContext(), u);
			
			adapter.notifyDataSetChanged();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		myNumber = preferences.getString("userMobNo", null);
		if (myNumber == null) {
			Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra("reg", true);
			startActivity(intent);
		}
		datasource = new UsersDataSource(this);
		datasource.open();
		datasourceForRead = new UsersDataSource(this);
		datasourceForRead.openForRead();
		usersToDisplay = new ArrayList<User>();
		adapter = new UserListingAdapter(this, usersToDisplay);
		setListAdapter(adapter);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mylist);
		listView = getListView();
		listView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> myAdapter, View myView,
					int myItemInt, long mylng) {
				User clickedUser = (User) (listView
						.getItemAtPosition(myItemInt));
				Bundle bundle = new Bundle();
				bundle.putString("userName", clickedUser.getName());
				bundle.putString("userNumber", clickedUser.getId());
				Intent intent = new Intent(myView.getContext(),
						ChatFragmentActivity.class);
				intent.putExtras(bundle);
				startActivity(intent);
			}
		});
		new InitializeUsersTask().execute();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.refresh:
			Toast.makeText(this, "Refreshing...This might take a few minutes",
					Toast.LENGTH_SHORT).show();
			new RefreshTask().execute();
			return true;

		case R.id.refreshRegistered:
			new RefreshRegisteredUsersTask().execute();
			return true;

		case R.id.register:
			Intent intent = new Intent(context, MainActivity.class);
			intent.putExtra("reg", true);
			startActivity(intent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		registerReceiver(mMessageReceiver, new IntentFilter(
				"com.chat.contactsRefreshEvent"));
		registerReceiver(messageReceived, new IntentFilter("com.example.testgcm.Message"));
		new RefreshRegisteredUsersTask().execute();
		super.onResume();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(mMessageReceiver);
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.user_list_activity, menu);
		return true;
	}

	private Cursor getContacts() {
		Uri uri = ContactsContract.Contacts.CONTENT_URI;
		String[] projection = new String[] { ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME,
				ContactsContract.Contacts.HAS_PHONE_NUMBER };
		String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '"
				+ ("1") + "'";
		String[] selectionArgs = null;
		String sortOrder = ContactsContract.Contacts.DISPLAY_NAME
				+ " COLLATE LOCALIZED ASC";
		return managedQuery(uri, projection, selection, selectionArgs,
				sortOrder);
	}

	private List<User> getContactsFromPhoneBook() {
		List<User> users = new ArrayList<User>();
		Cursor cursor = getContacts();
		ContentResolver cr = getContentResolver();

		while (cursor.moveToNext()) {
			String displayName = cursor.getString(cursor
					.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
			String hasPhone = cursor.getString(cursor
					.getColumnIndex(ContactsContract.Data.HAS_PHONE_NUMBER));
			String phone = null;
			boolean isAdd = false;
			if (Integer.parseInt(hasPhone) == 1) {
				String contactId = cursor.getString(cursor
						.getColumnIndex(ContactsContract.Contacts._ID));
				Cursor phones = cr.query(Phone.CONTENT_URI, null,
						Phone.CONTACT_ID + " = " + contactId, null, null);
				while (phones.moveToNext()) {
					String number = phones.getString(phones
							.getColumnIndex(Phone.NUMBER));
					int type = phones.getInt(phones.getColumnIndex(Phone.TYPE));
					switch (type) {
					case Phone.TYPE_HOME:
						continue;
					case Phone.TYPE_MOBILE:
						if (number.startsWith("+")) {
							phone = number.replace(" ", "");
							phone = phone.replace("-", "");
							isAdd = true;
						} else {
							continue;
						}
						break;
					case Phone.TYPE_WORK:
						// do something with the Work number here...
						continue;
					}
				}
				phones.close();

			} else
				continue;

			if (isAdd) {
				User user = new User();
				user.setName(displayName);
				user.setId(phone);
				user.setStatus("unregistered");
				users.add(user);
				datasource.createUser(user);
			}
		}
		return users;
	}

	private void refreshUsers() {
		datasource.deleteAllUsers();
		List<User> pbUsers = getContactsFromPhoneBook();
		for (User user : pbUsers) {
			datasource.createUser(user);
		}
		updateRegisteredUsers();
		usersToDisplay.clear();
		usersToDisplay.addAll(datasourceForRead.getRegisteredUsers());
	}

	private void updateRegisteredUsers() {
		JSONObject obj = new JSONObject();
		JSONArray arr = new JSONArray();
		try {

			obj.put("devices", arr);
			for (User user : datasourceForRead.getAllUsers()) {
				arr.put(user.getId());
			}
			String regUsersString = Utils.sendToServer(obj,
					"VerifyRegisteredDevices");
			JSONArray resArr = new JSONArray();
			JSONObject resObj = new JSONObject(regUsersString);
			resArr = resObj.getJSONArray("devices");
			datasource.markAllAsUnregistered();
			for (int i = 0; i < resArr.length(); i++) {
				datasource.markUserAsRegistered(resArr.get(i).toString());
			}
		} catch (Exception e) {
			Log.e("CHAT_APP", "Error in updating registered users from server");
		}
	}

	@Override
	protected void onDestroy() {
		datasource.close();
		datasourceForRead.close();
		super.onDestroy();
	}

	private class RefreshTask extends AsyncTask<Void, Void, Void> {

		private ProgressDialog dialog;

		public RefreshTask() {
			dialog = new ProgressDialog(context);
		}

		@Override
		protected void onPreExecute() {
			dialog.setMessage("Refreshing contact list using phone contacts");
			dialog.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {
			refreshUsers();
			Intent in = new Intent();
			in.setAction("com.chat.contactsRefreshEvent");
			sendBroadcast(in);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			dialog.dismiss();
			Log.d("CHAT_APP", "Contacts refreshed");
			Toast.makeText(context, "Done", Toast.LENGTH_SHORT);
			super.onPostExecute(result);
		}

		@Override
		protected void onCancelled() {
			dialog.dismiss();
			super.onCancelled();
		}
	}

	private class RefreshRegisteredUsersTask extends
			AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			Intent in = new Intent();
			in.setAction("com.chat.contactsRefreshEvent");
			sendBroadcast(in);
			updateRegisteredUsers();
			usersToDisplay.clear();
			usersToDisplay.addAll(datasourceForRead.getRegisteredUsers());
			Log.d("CHAT_APP", "inside RefreshRegisteredUsersTask");
			sendBroadcast(in);
			return null;
		}
	}

	private class InitializeUsersTask extends
			AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			Intent in = new Intent();
			in.setAction("com.chat.contactsRefreshEvent");
			usersToDisplay.clear();
			usersToDisplay.addAll(datasourceForRead.getRegisteredUsers());
			Log.d("CHAT_APP", "Initializing users");
			sendBroadcast(in);
			return null;
		}
		
		
	}
}
