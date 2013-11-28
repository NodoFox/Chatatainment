package com.chatatainment.game;

import java.io.Serializable;

public class TicTacToe {
	private int[][] state;
	private int nextTurn;
	public static int STATE_O = 0;
	public static int STATE_X = 1;
	public static int STATE_EMPTY = -1;
	public static int STATE_GAME_DRAW = -2;
	private boolean isMyTurn;
	
	public boolean isMyTurn() {
		return isMyTurn;
	}

	public void setMyTurn(boolean isMyTurn) {
		this.isMyTurn = isMyTurn;
	}

	public int getNextTurn() {
		return nextTurn;
	}

	public void setNextTurn(int nextTurn) {
		this.nextTurn = nextTurn;
	}

	public TicTacToe() {
		state = new int[3][3];
		resetGame();
	}

	public void resetGame() {
		for (int i = 0; i < state.length; i++) {
			for (int j = 0; j < state[i].length; j++) {
				state[i][j] = STATE_EMPTY;
			}
		}
		nextTurn = STATE_X;
	}

	public int getWinner() {
		if (checkIfGameDraw()) {
			return STATE_GAME_DRAW;
		}
		if (checkIfPlayerWins(STATE_X)) {
			return STATE_X;
		} else if (checkIfPlayerWins(STATE_O)) {
			return STATE_O;
		} else {
			return STATE_EMPTY;
		}
	}

	private boolean checkIfGameDraw() {
		for (int i = 0; i < state.length; i++) {
			for (int j = 0; j < state[i].length; j++) {
				if (state[i][j] == STATE_EMPTY) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 
	 * @param player
	 *            either STATE_O OR STATE_X
	 */
	private boolean checkIfPlayerWins(int player) {
		if (state[0][0] == player && state[0][1] == player
				&& state[0][2] == player)
			return true;
		if (state[1][0] == player && state[1][1] == player
				&& state[1][2] == player)
			return true;
		if (state[2][0] == player && state[2][1] == player
				&& state[2][2] == player)
			return true;
		if (state[0][0] == player && state[1][0] == player
				&& state[2][0] == player)
			return true;
		if (state[0][1] == player && state[1][1] == player
				&& state[2][1] == player)
			return true;
		if (state[0][2] == player && state[1][2] == player
				&& state[2][2] == player)
			return true;
		if (state[0][0] == player && state[1][1] == player
				&& state[2][2] == player)
			return true;
		if (state[0][2] == player && state[1][1] == player
				&& state[2][0] == player)
			return true;

		return false;
	}

	public boolean makeMove(int x, int y, int player) {
		if (state[x][y] == STATE_EMPTY && nextTurn == player) {
			state[x][y] = player;
			nextTurn = (nextTurn == STATE_O) ? STATE_X : STATE_O;
			return true;
		}
		return false;
	}

	public boolean makeMove(int x, int y) {
		if (state[x][y] == STATE_EMPTY && getWinner()==STATE_EMPTY) {
			state[x][y] = nextTurn;
			nextTurn = (nextTurn == STATE_O) ? STATE_X : STATE_O;
			return true;
		}
		return false;
	}

	public int[][] getState() {
		return state;
	}

	public void setState(int[][] state) {
		this.state = state;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < state.length; i++) {
			for (int j = 0; j < state[i].length; j++) {
				sb.append(state[i][j] + " ");
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	// public static void main(String args[]) {
	// TicTacToe t = new TicTacToe();
	// int[][] state = { { 0, 0, -1 }, { 1, 0, 0 }, { 1, -1, 0 } };
	// t.setState(state);
	// System.out.println(t);
	// System.out.println(t.getWinner());
	// }
	
	public static class Move implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = 6241266743610859093L;
		private int x;
		private int y;
		private int turn;
		

		private String to;
		private String from;
		@Override
		public String toString(){
			return "turn:"+turn+":"+x+":"+y;
		}
		
		public Move(){};

		public String getTo() {
			return to;
		}

		public void setTo(String to) {
			this.to = to;
		}

		public String getFrom() {
			return from;
		}

		public void setFrom(String from) {
			this.from = from;
		}

		public int getX() {
			return x;
		}

		public void setX(int x) {
			this.x = x;
		}

		public int getY() {
			return y;
		}

		public void setY(int y) {
			this.y = y;
		}

		public int getTurn() {
			return turn;
		}

		public void setTurn(int turn) {
			this.turn = turn;
		}

		public Move(int turn, int x, int y) {
			this.turn = turn;
			this.x = x;
			this.y = y;
		}
	}
}
