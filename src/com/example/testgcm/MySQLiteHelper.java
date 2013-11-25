package com.example.testgcm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

	public static final String TABLE_USERS = "user";
	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_PIC = "pic";
	public static final String COLUMN_STATUS = "status";

	private static final String DATABASE_NAME = "messenger.db";
	private static final int DATABASE_VERSION = 7;

	// Database creation sql statement
	private static final String DATABASE_CREATE = "create table " + TABLE_USERS
			+ "(" + COLUMN_ID + " text primary key, " + COLUMN_NAME
			+ " text not null, " + COLUMN_PIC + " text, " + COLUMN_STATUS
			+ " text);";

	private static final String DATABASE_CREATE1 = " CREATE TABLE message (_id integer primary key autoincrement, msg_from text not null, msg_to text not null, timestamp text not null, msg text, sent integer, delivered integer);";
	
	public MySQLiteHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		database.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
		database.execSQL(DATABASE_CREATE);
		database.execSQL(DATABASE_CREATE1);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.w(MySQLiteHelper.class.getName(),
				"Upgrading database from version " + oldVersion + " to "
						+ newVersion + ", which will destroy all old data");
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
		db.execSQL("DROP TABLE IF EXISTS message");
		onCreate(db);
	}

}
