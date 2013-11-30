package com.example.testgcm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import com.chatatainment.database.MessageDataSource;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ChatFragment extends Fragment {
	private String myNumber;
	private String userName;
	private String userNumber;
	private List<Message> messages;
	private MessageAdapter adapter;
	static Boolean isActive = false;
	
	MessageDataSource dsForRead = null;
	MessageDataSource ds = null;
	Activity parentActivity = null;
	View fragmentView = null;

	BroadcastReceiver messageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			refreshDataSet();
		}

	};

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fragmentView = inflater.inflate(R.layout.chat_layout, container, false);
		parentActivity = getActivity();
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		myNumber = preferences.getString("userMobNo", null);
		if (myNumber == null) {
			Intent intent = new Intent(parentActivity, MainActivity.class);
			intent.putExtra("reg", true);
			startActivity(intent);
		}
		ds = new MessageDataSource(parentActivity);
		dsForRead = new MessageDataSource(parentActivity);
		ds.open();
		dsForRead.open();
		Bundle bundle = parentActivity.getIntent().getExtras();
		userName = bundle.getString("userName");
		userNumber = bundle.getString("userNumber");
		parentActivity.setTitle(userName);
		dsForRead.getMessagesForUsers(myNumber, userNumber, messages);

		Button button = (Button) fragmentView.findViewById(R.id.button1);
		ListView lView = (ListView) fragmentView.findViewById(R.id.listView1);
		lView.setAdapter(adapter = new MessageAdapter(parentActivity, messages));
		lView.setSelection(messages.size() - 1);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText editText = (EditText) fragmentView
						.findViewById(R.id.editText1);
				String text = editText.getText().toString();
				if ("".equals(text))
					return;
				editText.setText("");
				Message msg2Send = new Message();
				msg2Send.setFrom(myNumber);
				msg2Send.setTo(userNumber);
				msg2Send.setIsMine(true);
				msg2Send.setMsg(text);
				msg2Send.setTimestamp(new Date());
				addNewMessage(msg2Send);
				new SendMessageTask().execute(msg2Send);
			}
		});
		dsForRead.open();
		dsForRead.markAllRead(myNumber);
		return fragmentView;
	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// // Inflate the menu; this adds items to the action bar if it is present.
	// getMenuInflater().inflate(R.menu.activity_chat, menu);
	// return true;
	// }

	public String getMyNumber() {
		return myNumber;
	}

	public void setMyNumber(String myNumber) {
		this.myNumber = myNumber;
	}

	public String getUserNumber() {
		return userNumber;
	}
	

	public void setUserNumber(String userNumber) {
		this.userNumber = userNumber;
	}

	void addNewMessage(Message m) {
		messages.add(m);
		adapter.notifyDataSetChanged();
		((ListView) fragmentView.findViewById(R.id.listView1))
				.setSelection(messages.size() - 1);
	}

	void refreshDataSet() {
		//dsForRead.getUnviewedMessageMap(myNumber);
		new RefreshListsTask().execute();
	}
	
	

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	public void onResume() {
		isActive = true;
		parentActivity.registerReceiver(messageReceiver, new IntentFilter(
				"com.example.testgcm.Message"));
		super.onResume();
	}

	@Override
	public void onPause() {
		isActive = false;
		parentActivity.unregisterReceiver(messageReceiver);
		super.onPause();
	}

	@Override
	public void onDestroy() {
		ds.close();
		dsForRead.close();
		super.onDestroy();
	}

	private class RefreshListsTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// TODO change this to preference number
			dsForRead.getMessagesForUsers(myNumber, userNumber, messages);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			adapter.notifyDataSetChanged();
			ListView list = ((ListView) fragmentView
					.findViewById(R.id.listView1));
			list.refreshDrawableState();
			list.invalidate();
			list.setSelection(messages.size() - 1);
			super.onPostExecute(result);
		}

	}

	public class SendMessageTask extends AsyncTask<Message, Void, String> {
		private Message message = null;

		@Override
		protected String doInBackground(Message... obj) {
			message = obj[0];
			try {
				JSONObject sendMsgObj = new JSONObject();
				try {
					sendMsgObj.put("msg", obj[0].getMsg());
					sendMsgObj.put("from", obj[0].getFrom());
					sendMsgObj.put("to", obj[0].getTo());
					sendMsgObj.put("type", obj[0].getType());
				} catch (Exception e) {
					e.printStackTrace();
				}
				message.setTimestamp(new Date());
				message.setIsMine(true);
				message.setSent(true);
				ds.saveMessage(message);
				JSONObject resObj = null;
				if(Message.Types.NEW_GAME_REQUEST.equals(message.getType())){
					//TODO Send new game request to server
					resObj = new JSONObject(Utils.sendToServer(
							sendMsgObj, "SendMessage"));
				}else{
					resObj = new JSONObject(Utils.sendToServer(
							sendMsgObj, "SendMessage"));					
				}
				
			} catch (Exception e) {
				Log.e("CHATSERVER", "" + e.getMessage());
			} finally {
			}
			return message.getId() + "";
		}

	}

	public static Boolean getIsActive() {
		return isActive;
	}

	public static void setIsActive(Boolean isActive) {
		ChatFragment.isActive = isActive;
	}

	
	public ChatFragment() {
		messages = new ArrayList<Message>();
	}

	public List<Message> getMessages() {
		return messages;
	}

	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}

	public MessageAdapter getAdapter() {
		return adapter;
	}

	public void setAdapter(MessageAdapter adapter) {
		this.adapter = adapter;
	}

	public BroadcastReceiver getMessageReceiver() {
		return messageReceiver;
	}

	public void setMessageReceiver(BroadcastReceiver messageReceiver) {
		this.messageReceiver = messageReceiver;
	}

}
