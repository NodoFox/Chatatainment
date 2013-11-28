package com.example.testgcm;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.chatatainment.game.TicTacToe;

public class GameFragment extends Fragment {
	private Button buttons[][];
	private Button resetButton;
	private TicTacToe game;
	private Activity parentActivity = null;
	private View fragmentView = null;
	private int myState = 0;
	private String myNumber = null;
	private String userNumber = null;
	private String userName = null;
	private GameDataSource gameDSForRead = null;
	private GameDataSource gameDSForWrite = null;
	

	BroadcastReceiver gameMoveReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			TicTacToe.Move move = (TicTacToe.Move) intent.getExtras()
					.getSerializable("move");
			int x = move.getX(), y = move.getY(), winner = TicTacToe.STATE_EMPTY;
			game.setNextTurn(move.getTurn());
			if (game.makeMove(x, y)) {
				setMyState(game.getNextTurn());
				updateButtonsWithGameState();
				winner = game.getWinner();
				if (winner != TicTacToe.STATE_EMPTY) {
					String message = (winner == TicTacToe.STATE_X) ? "X wins!"
							: (winner == TicTacToe.STATE_O) ? "O Wins"
									: "Game draw";
					Toast.makeText(parentActivity.getApplicationContext(),
							message, Toast.LENGTH_LONG).show();
					updateButtonsWithGameState();
				}
			}

		}

	};

	@Override
	public void onPause() {
		parentActivity.unregisterReceiver(gameMoveReceiver);
		saveGameStateToDatabase();
		super.onPause();
	}

	@Override
	public void onResume() {
		parentActivity.registerReceiver(gameMoveReceiver, new IntentFilter(
				"com.example.testgcm.GameMove"));
		loadGameStateFromDatabase();
		super.onResume();
	}
	
	

	@Override
	public void onDestroy() {
		if(gameDSForRead!=null){
			gameDSForRead.close();
		}
		if(gameDSForWrite!=null){
			gameDSForWrite.close();
		}
		super.onDestroy();
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getMyNumber() {
		return myNumber;
	}

	public void setMyNumber(String myNumber) {
		this.myNumber = myNumber;
	}

	public String getUserNumber() {
		return userNumber;
	}

	public void setUserNumber(String userNumber) {
		this.userNumber = userNumber;
	}

	public TicTacToe getGame() {
		return game;
	}

	public void setGame(TicTacToe game) {
		this.game = game;
	}

	public int getMyState() {
		return myState;
	}

	public void setMyState(int myState) {
		this.myState = myState;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fragmentView = inflater.inflate(R.layout.fragment_tictactoe, container,
				false);
		parentActivity = getActivity();
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		myNumber = preferences.getString("userMobNo", null);
		if (myNumber == null) {
			Intent intent = new Intent(parentActivity, MainActivity.class);
			intent.putExtra("reg", true);
			startActivity(intent);
		}
		Bundle bundle = parentActivity.getIntent().getExtras();
		userName = bundle.getString("userName");
		userNumber = bundle.getString("userNumber");
		gameDSForRead = new GameDataSource(parentActivity);
		gameDSForRead.openForRead();
		gameDSForWrite = new GameDataSource(parentActivity);
		gameDSForWrite.open();
		
		buttons = new Button[3][3];
		buttons[0][0] = (Button) fragmentView.findViewById(R.id.button1);
		buttons[0][1] = (Button) fragmentView.findViewById(R.id.button2);
		buttons[0][2] = (Button) fragmentView.findViewById(R.id.button3);
		buttons[1][0] = (Button) fragmentView.findViewById(R.id.button4);
		buttons[1][1] = (Button) fragmentView.findViewById(R.id.button5);
		buttons[1][2] = (Button) fragmentView.findViewById(R.id.button6);
		buttons[2][0] = (Button) fragmentView.findViewById(R.id.button7);
		buttons[2][1] = (Button) fragmentView.findViewById(R.id.button8);
		buttons[2][2] = (Button) fragmentView.findViewById(R.id.button9);
		resetButton = (Button) fragmentView.findViewById(R.id.resetGameButton);
		game = new TicTacToe();
		
		updateButtonsWithGameState();
		attachClickHandlers();
		return fragmentView;
	}

	private void updateButtonsWithGameState() {
		int[][] state = game.getState();
		for (int i = 0; i < state.length; i++) {
			for (int j = 0; j < state[i].length; j++) {
				buttons[i][j].setText((state[i][j] == TicTacToe.STATE_X) ? "X"
						: (state[i][j] == TicTacToe.STATE_O) ? "O" : "");
			}
		}
	}

	private void attachClickHandlers() {
		for (int i = 0; i < buttons.length; i++) {
			for (int j = 0; j < buttons[i].length; j++) {
				buttons[i][j].setTag((i) * 3 + j);
				buttons[i][j].setOnClickListener(buttonClickHandler);
			}
		}

		resetButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				game.resetGame();
				updateButtonsWithGameState();
			}
		});
	}

	View.OnClickListener buttonClickHandler = new View.OnClickListener() {
		public void onClick(View v) {
			Button button = ((Button) v);
			int x = (Integer) button.getTag() / 3, y = (Integer) button
					.getTag() % 3, winner = TicTacToe.STATE_EMPTY;

			if (game.getNextTurn() == getMyState()) {
				if (game.makeMove(x, y)) {
					TicTacToe.Move move = new TicTacToe.Move(getMyState(), x, y);
					move.setFrom(myNumber);
					move.setTo(userNumber);
					new GameMessageSenderTask().execute(move);
					updateButtonsWithGameState();
					winner = game.getWinner();
					if (winner != TicTacToe.STATE_EMPTY) {
						String message = (winner == TicTacToe.STATE_X) ? "X wins!"
								: (winner == TicTacToe.STATE_O) ? "O Wins"
										: "Game draw";
						Toast.makeText(parentActivity.getApplicationContext(),
								message, Toast.LENGTH_LONG).show();
						updateButtonsWithGameState();
					}
				}
			} else {
				Toast.makeText(parentActivity.getApplicationContext(),
						"Not your turn", Toast.LENGTH_SHORT).show();
			}

		}
	};
	
	
	public void saveGameStateToDatabase(){
		int[][] state = game.getState();
		int nextTurn = game.getNextTurn();
		JSONObject jsonObject = new JSONObject();
		JSONObject stateObj = new JSONObject();
		//userNumber
		try{
			for(int i=0;i<3;i++){
				JSONArray arr = new JSONArray();
				for(int j=0;j<3;j++){
					arr.put(state[i][j]);
				}
				stateObj.put(""+i, arr);
			}
			jsonObject.put("state", stateObj);
			jsonObject.put("nextTurn", nextTurn);
			Log.d("CHAT_APP",jsonObject.toString());
		}catch(Exception e){
			Log.e("CHAT_APP", e.getMessage());
		}
		
		gameDSForWrite.saveGameState(jsonObject, userNumber);
		
	}
	
	public void loadGameStateFromDatabase(){
		JSONObject gameState= gameDSForRead.getGameState(userNumber);
		int state[][] = new int[3][3];
		if(gameState!=null){
			try {
				JSONObject stateObj = gameState.getJSONObject("state");
				for(int i = 0 ;i <3;i++){
					JSONArray arr = stateObj.getJSONArray(""+i);
					for(int j=0;j<3;j++){
						state[i][j] = arr.getInt(j);
					}
				}
				game.setState(state);
				game.setNextTurn(gameState.getInt("nextTurn"));
				Log.d("CHAT_APP",gameState.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updateButtonsWithGameState();
	}

}
