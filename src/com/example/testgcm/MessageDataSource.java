package com.example.testgcm;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

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
		return list;
	}

}
