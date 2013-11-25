package com.example.testgcm;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class UsersDataSource {

	// Database fields
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;
	private String[] allColumns = { MySQLiteHelper.COLUMN_ID,
			MySQLiteHelper.COLUMN_NAME, MySQLiteHelper.COLUMN_PIC,
			MySQLiteHelper.COLUMN_STATUS };

	public UsersDataSource(Context context) {
		dbHelper = new MySQLiteHelper(context);
	}

	public void open() throws SQLException {
		database = dbHelper.getWritableDatabase();
	}

	public void openForRead() {
		database = dbHelper.getReadableDatabase();
	}

	public void close() {
		dbHelper.close();
	}

	public long createUser(User user) {
		try {
			ContentValues values = new ContentValues();
			values.put(MySQLiteHelper.COLUMN_ID, user.getId());
			values.put(MySQLiteHelper.COLUMN_NAME, user.getName());
			// values.put(MySQLiteHelper.COLUMN_PIC, user.getPic());
			values.put(MySQLiteHelper.COLUMN_STATUS, user.getStatus());
			long insertId = database.insert(MySQLiteHelper.TABLE_USERS, null,
					values);
			// Cursor cursor = database.query(MySQLiteHelper.TABLE_USERS,
			// allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
			// null, null, null);
			// cursor.moveToFirst();
			// Comment newComment = cursorToComment(cursor);
			// cursor.close();
			Log.d("CHAT_APP", "User added: " + user.getName()+" : "+user.getId());
			return insertId;
		} catch (Exception e) {
			Log.e("CHAT_APP", ""+e.getMessage());
			return -1;
		}
	}
	
	public void deleteAllUsers(){
		database.delete(MySQLiteHelper.TABLE_USERS, null, null);
	}
	
	public void markUserAsRegistered(String userId){
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_STATUS, "registered");
		database.update(MySQLiteHelper.TABLE_USERS, values, "_id = '"+userId+"'", null);
		Log.d("CHAT_APP","User marked as registered: "+userId);
	}
	
	public void markAllAsUnregistered(){
		ContentValues values = new ContentValues();
		values.put(MySQLiteHelper.COLUMN_STATUS, "unregistered");
		database.update(MySQLiteHelper.TABLE_USERS, values, null, null);
		Log.d("CHAT_APP","All users invalidated");
	}

	public List<User> getAllUsers() {
		List<User> users = new ArrayList<User>();

		Cursor cursor = null;
		try {
			cursor = database.query(MySQLiteHelper.TABLE_USERS, allColumns,
					null, null, null, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			User user = cursorToUser(cursor);
			users.add(user);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return users;
	}
	
	
	
	public List<User> getRegisteredUsers() {
		List<User> users = new ArrayList<User>();
		
		Cursor cursor = null;
		try {
			cursor = database.query(MySQLiteHelper.TABLE_USERS, allColumns,
					MySQLiteHelper.COLUMN_STATUS+"='registered'", null, null, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if(cursor==null)
			return users;
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			User user = cursorToUser(cursor);
			users.add(user);
			cursor.moveToNext();
		}
		// Make sure to close the cursor
		cursor.close();
		return users;
	}

	private User cursorToUser(Cursor cursor) {
		User user = new User();
		user.setId(cursor.getString(0));
		user.setName(cursor.getString(1));
		user.setPic(cursor.getString(2));
		user.setStatus(cursor.getString(3));
		return user;
	}
	
	public String getUserNameForMobileNumber(String userNumber){
		Cursor cursor = null;
		try {
			cursor = database.query(MySQLiteHelper.TABLE_USERS, allColumns,
					MySQLiteHelper.COLUMN_ID+"='"+userNumber+"'", null, null, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		cursor.moveToFirst();
		if(cursor.isAfterLast())
			return null;
		else{
			return cursorToUser(cursor).getName();
		}
	}
}