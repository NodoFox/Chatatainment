package com.example.testgcm;

import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class GameDataSource {
	private SQLiteDatabase database;
	private MySQLiteHelper dbHelper;

	

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

	public GameDataSource(Context ctx) {
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
	
	
	public boolean saveGameState(JSONObject gameState, String userNumber){
		try{
			ContentValues values = new ContentValues();
			values.put("user", userNumber);
			values.put("game_state", gameState.toString());
			if(!isGameStateAlreadyPresent(userNumber)){
				database.insert("games", null, values);
				return true;
			}else{
				database.update("games", values,"user='"+userNumber+"'", null);
				return true;
			}
		}catch(Exception e){
			Log.e("CHAT_APP", "saveGameState:" + e.getMessage());
			return false;
		}
	}
	
	public JSONObject getGameState(String userNumber){
		Cursor cursor = null;
		JSONObject gameState = null;
		try{
			cursor = database.query("games", null,
					"user='"+userNumber+"'", null, null, null, null);
			cursor.moveToFirst();
			if(cursor.isAfterLast())
				return null;
			else{
				gameState = new JSONObject(cursor.getString(2));
			}
			return gameState;
		}catch(Exception e){
			Log.e("CHAT_APP", "getGameState:" + e.getMessage());
			return null;
		}
	}
	
	public boolean isGameStateAlreadyPresent(String user){
		Cursor cursor = null;
		cursor = database.query("games", null,
				"user='"+user+"'", null, null, null, null);
		cursor.moveToFirst();
		if(cursor.isAfterLast())
			return false;
		return true;
	}

}
