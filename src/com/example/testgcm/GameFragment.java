package com.example.testgcm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.chatatainment.database.GameDataSource;
import com.chatatainment.database.GameDatabaseOperations;
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
	
	static Boolean isActive = false;
	
	

	public static Boolean getIsActive() {
		return isActive;
	}

	public static void setIsActive(Boolean isActive) {
		GameFragment.isActive = isActive;
	}

	public GameDataSource getGameDSForRead() {
		return gameDSForRead;
	}

	public void setGameDSForRead(GameDataSource gameDSForRead) {
		this.gameDSForRead = gameDSForRead;
	}

	public GameDataSource getGameDSForWrite() {
		return gameDSForWrite;
	}

	public void setGameDSForWrite(GameDataSource gameDSForWrite) {
		this.gameDSForWrite = gameDSForWrite;
	}

	BroadcastReceiver gameMoveReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			GameDatabaseOperations.loadGameStateFromDatabase(game, userNumber,
					gameDSForRead);
			updateButtonsWithGameState();
		}

	};

	@Override
	public void onPause() {
		isActive = false;
		parentActivity.unregisterReceiver(gameMoveReceiver);
		GameDatabaseOperations.saveGameStateToDatabase(game, userNumber,
				gameDSForWrite);
		super.onPause();
	}

	@Override
	public void onResume() {
		isActive = true;
		parentActivity.registerReceiver(gameMoveReceiver, new IntentFilter(
				"com.example.testgcm.GameMove"));
		GameDatabaseOperations.loadGameStateFromDatabase(game, userNumber,
				gameDSForRead);
		updateButtonsWithGameState();
		
		super.onResume();
	}

	@Override
	public void onDestroy() {
		if (gameDSForRead != null) {
			gameDSForRead.close();
		}
		if (gameDSForWrite != null) {
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
		if (!GameDatabaseOperations.loadGameStateFromDatabase(game, userNumber,
				gameDSForRead)) {
			game.setMyTurn(true);
		}
		Activity activity = getActivity();
		InputMethodManager inputMethodManager = (InputMethodManager)  activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
		try
		{
		    inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), 0);
		    Log.d("HIDEKEY","I'm here");
		}
		catch (Exception e)
		{
			Log.d("HIDEKEY","Exception");
		}
		updateButtonsWithGameState();
		attachClickHandlers();
		return fragmentView;
	}

	public void updateButtonsWithGameState() {
		int[][] state = game.getState();
		for (int i = 0; i < state.length; i++) {
			for (int j = 0; j < state[i].length; j++) {
				buttons[i][j].setText((state[i][j] == TicTacToe.STATE_X) ? "X"
						: (state[i][j] == TicTacToe.STATE_O) ? "O" : "");
			}
		}
		int winner = TicTacToe.STATE_EMPTY;
		TextView nextTurnView = (TextView) fragmentView
				.findViewById(R.id.nextTurn);
		TextView statusMessage = (TextView) fragmentView
				.findViewById(R.id.gameStatus);
		String nextTurn = game.getNextTurn() == TicTacToe.STATE_X ? "X"	: "O";
		nextTurnView.setText("");
		if (game.isMyTurn()) {
			statusMessage.setText("Your("+nextTurn+") turn to play");
			ChatFragmentActivity.globalActionBar.getTabAt(1).setText("Game *");
		} else {
			statusMessage.setText("Waiting for " + userName
					+ " to make a move...");
			ChatFragmentActivity.globalActionBar.getTabAt(1).setText("Game");
		}
		winner = game.getWinner();
		if (winner != TicTacToe.STATE_EMPTY) {
			
			String message = null;
			/*if(game.isMyTurn()){
				message = (winner == game.getNextTurn() && winner == TicTacToe.STATE_X) ? "You win!"
						: (winner == TicTacToe.STATE_O && !(winner==game.getNextTurn())) ? userName+" Wins" : "Game draw";
			}else {
				message = (winner == game.getNextTurn() && winner == TicTacToe.STATE_X) ? "You win!"
						: (winner == TicTacToe.STATE_O && !(winner==game.getNextTurn())) ? userName+" Wins" : "Game draw";
			}*/
			Vibrator v =(Vibrator) parentActivity.getSystemService(Context.VIBRATOR_SERVICE);
			if(winner == TicTacToe.STATE_X){
				if(!game.isMyTurn()){
					message = "*****You Win!*****";
					v.vibrate(GameFragment.getIsActive() ? 1000: 5);
				}else{
					message = "You Lose!";
					v.vibrate(GameFragment.getIsActive() ? 500 : 100);
				}
			}else if(winner == TicTacToe.STATE_O){
				if(!game.isMyTurn()){
					message="*****You Win!*****";
					v.vibrate(GameFragment.getIsActive() ? 1000: 5);
				}else{
					message="You Lose!";
					v.vibrate(GameFragment.getIsActive() ? 500 : 100);
				}
			}else{
				message = "Game Draw!";
				v.vibrate(GameFragment.getIsActive() ? 500 : 100);
			}
				
				
			//Toast.makeText(getActivity(), message,Toast.LENGTH_LONG).show();
			statusMessage.setText(message);
			ChatFragmentActivity.globalActionBar.getTabAt(1).setText("Game");
			// Vibrate for 500 milliseconds
			
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
				GameDatabaseOperations.saveGameStateToDatabase(game,
						userNumber, gameDSForWrite);
				TicTacToe.Move move = new TicTacToe.Move();
				move.setTo(userNumber);
				move.setFrom(myNumber);
				Vibrator vb =(Vibrator) parentActivity.getSystemService(Context.VIBRATOR_SERVICE);
				vb.vibrate(100);
				new NewGameCommandSenderTask().execute(move);
				updateButtonsWithGameState();
			}
		});
	}

	View.OnClickListener buttonClickHandler = new View.OnClickListener() {
		public void onClick(View v) {
			Button button = ((Button) v);
			int x = (Integer) button.getTag() / 3, y = (Integer) button
					.getTag() % 3;

			if (game.isMyTurn()) {
				if (game.makeMove(x, y)) {
					game.setMyTurn(false);
					TicTacToe.Move move = new TicTacToe.Move(getMyState(), x, y);
					move.setFrom(myNumber);
					move.setTo(userNumber);
					new GameMessageSenderTask().execute(move);
					GameDatabaseOperations.saveGameStateToDatabase(game,
							userNumber, gameDSForWrite);
					updateButtonsWithGameState();
				}
			} else {
				Toast.makeText(parentActivity.getApplicationContext(),
						"Not your turn", Toast.LENGTH_SHORT).show();
			}
		}
	};

}
