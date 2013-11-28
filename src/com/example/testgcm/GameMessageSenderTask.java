package com.example.testgcm;

import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.chatatainment.game.TicTacToe;

public class GameMessageSenderTask extends
		AsyncTask<TicTacToe.Move, Void, String> {
	

	@Override
	protected String doInBackground(TicTacToe.Move... obj) {
		
		try {
			JSONObject gameMsgObj = new JSONObject();
			try {
				gameMsgObj.put("turn", obj[0].getTurn());
				gameMsgObj.put("x", obj[0].getX());
				gameMsgObj.put("y", obj[0].getY());
				gameMsgObj.put("type", Message.Types.GAME_MOVE);
				gameMsgObj.put("to", obj[0].getTo());
				gameMsgObj.put("from", obj[0].getFrom());
			} catch (Exception e) {
				e.printStackTrace();
			}
			JSONObject resObj = null;
			// TODO Send new game request to server
			resObj = new JSONObject(Utils.sendToServer(gameMsgObj,
					"MakeMove"));
			Log.d("CHAT_APP", "Game move sent to server : "+resObj.toString());

		} catch (Exception e) {
			Log.e("CHATSERVER", "" + e.getMessage());
		} finally {
		}
		return "";
	}

}