package com.example.testgcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
	MessageDataSource ds;
	private SoundPool soundPool;
	private Boolean soundLoaded = false;
	private int soundId;

	@Override
	protected void onError(Context context, String errorId) {
		System.out.println("Error has occured: " + errorId);
	}

	@Override
	public void onCreate() {
		ds = new MessageDataSource(this);
		ds.open();
		soundPool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
		soundPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
					int status) {
				soundLoaded = true;
			}
		});
		soundId = soundPool.load(this, R.raw.msg, 1);
		super.onCreate();
	};

	@Override
	public void onDestroy() {
		ds.close();
	};

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.d("CHAT_APP", "message received");
		Bundle bundle = intent.getExtras();
		Message message = new Message();
		message.setMsg(bundle.getString("msg"));
		message.setFrom(bundle.getString("msg_from"));
		message.setTo(bundle.getString("msg_to"));
		message.setIsMine(false);
		try {
			message.setTimestamp(Utils.dateFormatISO8601.parse(bundle
					.getString("timestamp")));
		} catch (Exception e) {
			Log.e("CHAT_APP", "" + e.getMessage());
			e.printStackTrace();
		}
		ds.saveMessage(message);
		UsersDataSource uds = new UsersDataSource(this);
		UsersDataSource udsForWrite = new UsersDataSource(this);
		String userNumber = bundle.getString("msg_from");
		uds.open();
		String userName = uds.getUserNameForMobileNumber(userNumber);
		uds.close();
		if (userName == null) {
			userName = userNumber;
			udsForWrite.open();
			User newUser = new User();
			newUser.setId(userName);
			newUser.setName(userName);
			newUser.setStatus("registered");
			udsForWrite.createUser(newUser);
			udsForWrite.close();
			Log.d("CHAT_APP", "User Created : " + userName);
		}
		Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(ChatFragment.getIsActive() ? 100 : 1000);
		if (soundLoaded) {
			soundPool.play(soundId, 0.1f, 0.1f, 1, 0, 1f);
			Log.e("Test", "Played sound");
		}
		if (!ChatFragment.getIsActive()) {
			Intent resultIntent = new Intent(this, ChatFragmentActivity.class);
			resultIntent.putExtra("userNumber", userNumber);
			resultIntent.putExtra("userName", userName);
			resultIntent.putExtra("msg", message);
			PendingIntent pIntent = PendingIntent.getActivity(this, 0,
					resultIntent, 0);
			Notification noti = new NotificationCompat.Builder(this)
					.setContentText((String) bundle.get("msg"))
					.setContentTitle(userName).setContentIntent(pIntent)
					.setSmallIcon(R.drawable.ic_launcher).build();
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			noti.flags |= Notification.FLAG_AUTO_CANCEL;
			mNotificationManager.notify(userNumber, 0, noti);
		}
		Intent msgIntent = new Intent();
		msgIntent.setAction("com.example.testgcm.Message");
		msgIntent.putExtra("msg", (String) bundle.get("msg"));
		sendBroadcast(msgIntent);
	}

	@Override
	protected void onRegistered(Context context, String regId) {
		Intent intent = new Intent();
		intent.setAction("GCMOnRegisterBroadcast");
		intent.putExtra("regId", regId);
		sendBroadcast(intent);

	}

	@Override
	protected void onUnregistered(Context context, String regId) {
		System.out.println("Unregistered ID: " + regId);

	}

}
