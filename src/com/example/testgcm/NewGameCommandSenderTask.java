package com.example.testgcm;

import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

import com.chatatainment.game.TicTacToe;

public class NewGameCommandSenderTask extends
		AsyncTask<TicTacToe.Move, Void, String> {
	

	@Override
	protected String doInBackground(TicTacToe.Move... obj) {
		
		try {
			JSONObject gameMsgObj = new JSONObject();
			try {
				gameMsgObj.put("type", Message.Types.NEW_GAME);
				gameMsgObj.put("to", obj[0].getTo());
				gameMsgObj.put("from", obj[0].getFrom());
			} catch (Exception e) {
				e.printStackTrace();
			}
			JSONObject resObj = null;
			// TODO Send new game request to server
			resObj = new JSONObject(Utils.sendToServer(gameMsgObj,
					"NewGame"));
			Log.d("CHAT_APP", "New Game command sent to server : "+resObj.toString());

		} catch (Exception e) {
			Log.e("CHATSERVER", "" + e.getMessage());
		} finally {
		}
		return "";
	}

}