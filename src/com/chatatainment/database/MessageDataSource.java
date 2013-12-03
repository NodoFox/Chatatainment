package com.chatatainment.database;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import com.example.testgcm.ChatFragment;
import com.example.testgcm.Message;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class MessageDataSource {
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;

	private DateFormat dateFormatISO8601 = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public SQLiteDatabase getDatabase() {
		return database;
	}

	public void setDatabase(SQLiteDatabase database) {
		this.database = database;
	}

	public MySQLiteHelper getDbHelper() {
		return dbHelper;
	}

	public void setDbHelper(MySQLiteHelper dbHelper) {
		this.dbHelper = dbHelper;
	}

	public MessageDataSource(Context ctx) {
		dbHelper = new MySQLiteHelper(ctx);
	}

	public void open() throws SQLException {
		if (database == null)
			database = dbHelper.getWritableDatabase();
	}
	
	public void openForRead() throws SQLException {
		if (database == null)
			database = dbHelper.getReadableDatabase();
	}

	public void close() {

		dbHelper.close();
	}

	public Message saveMessage(Message message) {
		ContentValues values = new ContentValues();
		values.put("msg_from", message.getFrom());
		values.put("msg_to", message.getTo());
		values.put("msg", message.getMsg());
		ChatFragment currentChat = new ChatFragment();
		if(!ChatFragment.getIsActive()){
			values.put("view", 0);
		}else {
			if(false)//!currentChat.getUserNumber().equals(message.getFrom()))
				values.put("view", 0);
			else
				values.put("view", 1);
		}
		values.put("sent", message.getSent() == true ? 1 : 0);
		values.put("delivered", message.getDelivered() == true ? 1 : 0);
		values.put("timestamp",
				dateFormatISO8601.format(message.getTimestamp()));
		values.put("type",message.getType());
		long msgId = database.insert("message", null, values);
		message.setId(msgId);
		return message;
	}

	public List<Message> getMessagesForUsers(String me, String user,
			List<Message> list) {
		list.clear();
		String selection = "msg_from = '" + user + "' OR msg_to = '" + user
				+ "'";
		String orderBy = "_id";
		Cursor cursor = database.query("message", null, selection, null, null,
				null, orderBy);
		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			Message msg = new Message();
			msg.setId(cursor.getLong(0));
			msg.setFrom(cursor.getString(1));
			msg.setTo(cursor.getString(2));
			msg.setType(cursor.getString(7));
			try {
				msg.setTimestamp(dateFormatISO8601.parse(cursor.getString(3)));
			} catch (Exception e) {
				e.printStackTrace();
			}
			msg.setMsg(cursor.getString(4));
			msg.setSent(cursor.getInt(5) == 1 ? true : false);
			msg.setDelivered(cursor.getInt(6) == 1 ? true : false);
			if (msg.getFrom().equals(me))
				msg.setIsMine(true);
			else
				msg.setIsMine(false);
			list.add(msg);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		//getUnviewedMessageMap(me);
		return list;
	}
	
	public HashMap<String,String[]> getUnviewedMessageMap(String me){
		this.openForRead();
		Log.d("UNDREAD","Inside method for Number: "+me);
		HashMap<String,String[]> unreadUsers = new HashMap<String, String[]>();
		//Cursor cursor = database.query("message", new String[]{"msg_from","?"}, where,null, groupBy,null, null);
		Cursor cursor = database.rawQuery("SELECT msg_from, count(*),timestamp from message where msg_to='"+me+"' AND view='0' group by msg_from order by timestamp desc", null);
		Log.d("UNDREAD",cursor.getCount()+" COUNT");
		//String array[] = new String[2];
		String array[] = new String[2];
		if(cursor.moveToFirst()){
			do{	
				array[0] = cursor.getString(2);
				array[1] = cursor.getString(1);
				Log.d("UNREAD","Number "+cursor.getString(0)+ " Timestamps: "+array[0]+" Count: "+array[1]);
				unreadUsers.put(cursor.getString(0),array);
				
			}while(cursor.moveToNext());
		}
		cursor.close();
		return unreadUsers;
	}
	
	public HashMap<String,String> getMaxTimeStampsForAllUsers(String me){
		HashMap<String,String> maxTimeStamps = new HashMap<String, String>();
		//Cursor cursor = database.query("message", new String[]{"msg_from","?"}, where,null, groupBy,null, null);
		Cursor cursor = database.rawQuery("SELECT msg_from, count(*),timestamp from message where msg_to='"+me+"' group by msg_from order by timestamp desc", null);
		
		if(cursor.moveToFirst()){
			do{	
				
				Log.d("User Number",cursor.getString(0)+"==+=="+cursor.getString(2));
				maxTimeStamps.put(cursor.getString(0),cursor.getString(2));
				
			}while(cursor.moveToNext());
		}
		cursor.close();
		return maxTimeStamps;
	}
	public void markAllRead(String me, String user){
		database.execSQL("UPDATE message SET view='1' WHERE msg_to='"+me+"' AND msg_from='"+user+"'");
	}

}
