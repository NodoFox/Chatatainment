package com.example.testgcm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
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
		super.onPause();
	}

	@Override
	public void onResume() {
		parentActivity.registerReceiver(gameMoveReceiver, new IntentFilter(
				"com.example.testgcm.GameMove"));
		super.onResume();
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

}