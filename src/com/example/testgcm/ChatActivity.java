package com.example.testgcm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class ChatActivity extends Activity {
	private String myNumber;
	private String userName;
	private String userNumber;
	private List<Message> messages;
	private MessageAdapter adapter;
	static Boolean isActive = false;
	MessageDataSource dsForRead = null;
	MessageDataSource ds = null;

	BroadcastReceiver messageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			refreshDataSet();
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
		myNumber = preferences.getString("userMobNo", null);
		if(myNumber==null){
			Intent intent = new Intent(this,MainActivity.class);
			intent.putExtra("reg", true);
			startActivity(intent);
		}
		super.onCreate(savedInstanceState);
		ds = new MessageDataSource(this);
		dsForRead = new MessageDataSource(this);
		ds.open();
		dsForRead.open();
		Bundle bundle = getIntent().getExtras();
		userName = bundle.getString("userName");
		userNumber = bundle.getString("userNumber");
		setTitle(userName);
		dsForRead.getMessagesForUsers(myNumber, userNumber, messages);
		setContentView(R.layout.chat_layout);
		Button button = (Button) findViewById(R.id.button1);
		ListView lView = (ListView) findViewById(R.id.listView1);
		lView.setAdapter(adapter = new MessageAdapter(this, messages));
		ListView list = ((ListView) findViewById(R.id.listView1));
		list.setSelection(messages.size() - 1);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText editText = (EditText) findViewById(R.id.editText1);
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
				// ((ChatActivity) v.getContext()).addNewMessage(new
				// Message(text,
				// new Date(), true));
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_chat, menu);
		return true;
	}

	void addNewMessage(Message m) {
		messages.add(m);
		adapter.notifyDataSetChanged();
		((ListView) findViewById(R.id.listView1))
				.setSelection(messages.size() - 1);
	}

	void refreshDataSet() {
		new RefreshListsTask().execute();
	}

	@Override
	protected void onResume() {
		isActive = true;
		registerReceiver(messageReceiver, new IntentFilter(
				"com.example.testgcm.Message"));
		super.onResume();
	}

	@Override
	protected void onPause() {
		isActive = false;
		unregisterReceiver(messageReceiver);
		super.onPause();
	}

	@Override
	protected void onDestroy() {
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
			ListView list = ((ListView) findViewById(R.id.listView1));
			list.refreshDrawableState();
			list.invalidate();
			list.setSelection(messages.size() - 1);
			super.onPostExecute(result);
		}

	}

	private class SendMessageTask extends AsyncTask<Message, Void, String> {

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
				} catch (Exception e) {
					e.printStackTrace();
				}
				message.setTimestamp(new Date());
				message.setIsMine(true);
				message.setSent(true);
				ds.saveMessage(message);
				JSONObject resObj = new JSONObject(Utils.sendToServer(
						sendMsgObj, "SendMessage"));
			} catch (Exception e) {
				Log.e("CHATSERVER", ""+e.getMessage());
			} finally {
			}
			return message.getId() + "";
		}

	}

	public static Boolean getIsActive() {
		return isActive;
	}

	public static void setIsActive(Boolean isActive) {
		ChatActivity.isActive = isActive;
	}

	public ChatActivity() {
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
