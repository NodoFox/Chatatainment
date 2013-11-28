package com.chatatainment.database;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.chatatainment.game.TicTacToe;

public class GameDatabaseOperations {
	public static void saveGameStateToDatabase(TicTacToe ticTacToeGame,
			String userNumber, GameDataSource gdsForWrite) {
		int[][] state = ticTacToeGame.getState();
		int nextTurn = ticTacToeGame.getNextTurn();
		JSONObject jsonObject = new JSONObject();
		JSONObject stateObj = new JSONObject();
		// userNumber
		try {
			for (int i = 0; i < 3; i++) {
				JSONArray arr = new JSONArray();
				for (int j = 0; j < 3; j++) {
					arr.put(state[i][j]);
				}
				stateObj.put("" + i, arr);
			}
			jsonObject.put("state", stateObj);
			jsonObject.put("nextTurn", nextTurn);
			jsonObject.put("myTurn", ticTacToeGame.isMyTurn());
			Log.d("CHAT_APP", "Saving to database:" + jsonObject.toString());
		} catch (Exception e) {
			Log.e("CHAT_APP", e.getMessage());
		}
		gdsForWrite.saveGameState(jsonObject, userNumber);
	}

	public static boolean loadGameStateFromDatabase(TicTacToe ticTacToeGame,
			String userNumber, GameDataSource gds) {
		JSONObject gameState = gds.getGameState(userNumber);
		int state[][] = new int[3][3];
		if (gameState != null) {
			try {
				JSONObject stateObj = gameState.getJSONObject("state");
				for (int i = 0; i < 3; i++) {
					JSONArray arr = stateObj.getJSONArray("" + i);
					for (int j = 0; j < 3; j++) {
						state[i][j] = arr.getInt(j);
					}
				}
				ticTacToeGame.setState(state);
				ticTacToeGame.setNextTurn(gameState.getInt("nextTurn"));
				ticTacToeGame.setMyTurn(gameState.getBoolean("myTurn"));
				Log.d("CHAT_APP", gameState.toString());

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return true;
		}
		return false;

	}
}
