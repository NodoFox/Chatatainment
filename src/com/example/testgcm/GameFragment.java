package com.example.testgcm;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.chatatainment.game.TicTacToe;

public class GameFragment extends Fragment {
	Button buttons[][];
	Button resetButton;
	TicTacToe game;
	Activity parentActivity = null;
	View fragmentView = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fragmentView = inflater.inflate(R.layout.fragment_tictactoe, container, false);
		parentActivity = getActivity();
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
				buttons[i][j].setTag((i)*3+j);
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
			Button button = ((Button)v);
			int x = (Integer)button.getTag()/3,
				y = (Integer)button.getTag()%3,
				winner = TicTacToe.STATE_EMPTY;
			if(game.makeMove(x, y)){
				updateButtonsWithGameState();
				winner = game.getWinner();
				if(winner!=TicTacToe.STATE_EMPTY){
					String message = (winner==TicTacToe.STATE_X)?"X wins!":(winner==TicTacToe.STATE_O)?"O Wins":"Game draw";
					Toast.makeText(parentActivity.getApplicationContext(),message, Toast.LENGTH_LONG).show();
					updateButtonsWithGameState();
				}
			}
			
		}
	};



}
