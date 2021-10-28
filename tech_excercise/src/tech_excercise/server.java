package tech_excercise;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

;

public class server {
	private List<List<String>> game_board = null;
	private List<player_info> player_list = new ArrayList<player_info>();
	protected boolean game_ready = false;

	public static void main(String[] args) throws IOException {
		try {
			server game = new server();
			game.setUpGameBoard();
			HttpServer server = HttpServer.create(new InetSocketAddress(4500), 0);
			server.createContext("/game", new GameHandler(game));
			server.setExecutor(null);
			server.start();
			System.out.println("Server Running");
		} catch (Exception e) {
			System.out.print(e);
		}

	}

	public void setUpGameBoard() {
		this.game_board = Collections.synchronizedList(new ArrayList<>());

		for (int x = 0; x < 6; x++) {
			this.game_board.add(Arrays.asList("", "", "", "", "", "", "", "", ""));
		}
	}

	public List<List<String>> getGameBoard() {
		return this.game_board;
	}

	public boolean addMove(int move, player_info player) {

		int row = 0;

		for (int i = this.game_board.size() - 1; i >= 0; i--) {
			System.out.println(this.game_board.get(0).get(move - 1));
			if (this.game_board.get(i).get(move - 1).equals("")) {
				switch (player.getColour()) {
				case ("Blue"):
					this.game_board.get(i).set(move - 1, player.getPlayerShape());
					break;
				case ("Red"):
					this.game_board.get(i).set(move - 1, player.getPlayerShape());
					break;
				}
				row = i;
				System.out.println(row);
				break;

			}

		}

		boolean has_won = this.checkForWin(row, move, player.getColour(), player.getPlayerShape());
		System.out.println(has_won);
		return has_won;
	}

	public int getNumOfPlayers() {
		return this.player_list.size();
	}

	public boolean checkForWin(int row, int col, String player_colour, String shape) {

		String streak = String.format("%s%s%s%s%s", shape, shape, shape, shape, shape);

		if (this.getBackSlash(row, col - 1).indexOf(streak) >= 0 || this.getVerticalLine(col - 1).indexOf(streak) >= 0
				|| this.getForwardSlash(row, col - 1).indexOf(streak) >= 0
				|| getHorizontalLine(row, this.getGameBoard().get(row), shape)) {
			System.out.println("win");
			return true;
		}

		for (List<String> test : game_board) {
			System.out.println(test);
		}
		System.out.println("------------------------------------------");
		return false;

	}

	private boolean getHorizontalLine(int row, List<String> line, String shape) {

		int count = 0;
		for (String cell : line) {
			if (cell.equals(shape)) {
				count += 1;
			} else if (count == 5)
				break;
			else
				count = 0;
		}

		return (count >= 5);
	}

	private StringBuilder getVerticalLine(int col) {
		StringBuilder verticalLine = new StringBuilder(9);
		for (int i = 0; i < 6; i++) {
			this.game_board.get(i).get(col);
			verticalLine.append(this.game_board.get(i).get(col));
		}
		;
		return verticalLine;
	}

	private StringBuilder getForwardSlash(int row, int col) {
		StringBuilder forwardSlash = new StringBuilder();

		for (int h = 0; h < 6; h++) {
			int w = col + row - h;
			if (0 <= w && w < 9) {
				forwardSlash.append(game_board.get(h).get(w));
			}
		}
		return forwardSlash;
	}

	private StringBuilder getBackSlash(int row, int col) {
		StringBuilder backSlash = new StringBuilder();

		for (int h = 0; h < 6; h++) {
			int w = col - row + h;
			if (0 <= w && w < 9) {
				backSlash.append(game_board.get(h).get(w));
			}
		}
		return backSlash;
	}

	public List<player_info> getPlayerList() {
		return this.player_list;
	}

	public boolean getGameReady() {
		return this.getGameReady();
	}

	public boolean confirmDetails(HttpExchange t) {
		for (player_info player : this.getPlayerList()) {
			if (player.getName().equals(t.getRequestHeaders().getFirst("player"))) {
				return false;
			}
			if (player.getColour().equals(t.getRequestHeaders().getFirst("player"))) {
				return false;
			}
		}
		return true;
	}

	static class GameHandler implements HttpHandler {
		private server game;
		private boolean valid_details = false;

		public GameHandler(server game) {
			this.game = game;
		}

		public void setValidDetails(boolean validDetails) {
			this.valid_details = validDetails;
		}

		@Override
		public void handle(HttpExchange t) throws IOException {
			OutputStream os = t.getResponseBody();

			if (t.getRequestMethod().equals("POST")) {

				String response = " ";
				InputStreamReader reader = new InputStreamReader(t.getRequestBody());
				BufferedReader br = new BufferedReader(reader);
				String msg = br.readLine();
				switch (msg) {
				case ("quit"):
					for (player_info player : game.player_list) {
						if (player.getName().equals(t.getRequestHeaders().getFirst("player"))) {
							player.setState("quit");
						}
					}
					// t.sendResponseHeaders(200, response.length());
					// os.write(response.getBytes());
					// os.close();
					break;
				case ("get_status"):
					try {
						for (player_info player : game.player_list) {
							if (!t.getRequestHeaders().getFirst("player").equals(player.getName())) {
								response = player.getState();
								response += "-" + player.getTurn();
								response += "-" + player.getHasWon();
								response += "-" + player.getLastMove();
								response += "-";
								for (List<String> row : game.game_board) {
									response += row + "+";
								}

							}
						}

						t.sendResponseHeaders(200, response.length());
						os.write(response.getBytes());
						os.close();
					} catch (Exception e) {
						System.out.print(e);
					}
					break;

				case ("make_move"):
					try {
						Boolean has_won = false;

						for (player_info player : game.player_list) {
							if (t.getRequestHeaders().getFirst("player").equals(player.getName())) {

								has_won = game.addMove(Integer.parseInt(t.getRequestHeaders().getFirst("move")),
										player);
								player.setHasWon(has_won);
								player.setLastMove(Integer.parseInt(t.getRequestHeaders().getFirst("move")));
								player.setTurn(false);
							}
						}

						for (player_info player : game.player_list) {
							if (!t.getRequestHeaders().getFirst("player").equals(player.getName()))
								player.setTurn(true);
						}

						t.sendResponseHeaders(200, has_won.toString().length());
						os.write(has_won.toString().getBytes());
						os.close();
						break;
					}

					catch (Exception e) {
						System.out.print(e);
					}
				}
			}

			if (t.getRequestMethod().equals("GET")) {
				try {
					String response;

					if (t.getRequestHeaders().getFirst("new").equals("true")) {

						valid_details = true;
						player_info player = new player_info(t.getRequestHeaders().getFirst("player"),
								t.getRequestHeaders().getFirst("colour"), t.getRequestHeaders().getFirst("state"),
								false);
						game.player_list.add(player);
						if (player.getColour().equals("Red")) {
							player.setPlayerShape("X");
							player.setTurn(true);
						} else
							player.setPlayerShape("O");
					}

					if (game.getNumOfPlayers() < 2) {
						response = "NR";
					}

					else {
						response = "R";
						if (!game.game_ready) {
							game.setUpGameBoard();
							game.game_ready = true;
						}
					}

					t.sendResponseHeaders(200, response.length());
					os.write(response.getBytes());
					os.close();

				} catch (Exception e) {
					System.out.print(e);
				}
			}
		}
	}

}

class player_info {
	private String name;
	private String colour;
	private String state;
	private boolean turn;
	private String player_shape;
	private boolean has_won;
	private int lasMove;

	public player_info(String name, String colour, String State, boolean Turn) {
		this.name = name;
		this.colour = colour;
		this.state = State;
		this.turn = Turn;
		this.has_won = false;

	}

	public String getName() {
		return name;
	}

	public String getColour() {
		return this.colour;
	}

	public String getState() {
		return this.state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public void setTurn(boolean turn) {
		this.turn = turn;
	}

	public boolean getTurn() {
		return turn;
	}

	public String getPlayerShape() {
		return this.player_shape;
	}

	public void setPlayerShape(String shape) {
		this.player_shape = shape;
	}

	public void setHasWon(boolean result) {
		this.has_won = result;
	}

	public boolean getHasWon() {
		return this.has_won;
	}

	public void setLastMove(int lastMove) {
		this.lasMove = lastMove;
	}

	public int getLastMove() {
		return this.lasMove;
	}

}
