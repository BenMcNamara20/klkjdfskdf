package tech_excercise;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

;

public class server {
	private Socket client = null;
	private int port = 4500;
	private ServerSocket server;
	private server_thread game_thread;
	private DataOutputStream out = null;
	private List<Thread> players = null;
	private List<List<String>> game_board = null;
	private int number_of_players;
	private List<player_info> player_list = new ArrayList<player_info>();
	protected boolean game_ready = false;
	private String tests = "1";

	public static void main(String[] args) throws IOException {
		server game = new server();
		game.setUpGameBoard();
		HttpServer server = HttpServer.create(new InetSocketAddress(4500), 0);
		server.createContext("/game", new GameHandler(game));
		server.setExecutor(null);
		server.start();

	}

	public void setUpGameBoard() {
		this.game_board = Collections.synchronizedList(new ArrayList<>());

		for (int x = 0; x < 6; x++) {
			this.game_board.add(Arrays.asList("[]", "[]", "[]", "[]", "[]", "[]", "[]", "[]", "[]"));
		}
	}

	public List<List<String>> getGameBoard() {
		return this.game_board;
	}

	public boolean addMove(int move, player_info player) {

		
		boolean move_placed = false;
		int row = 0;
		
		/*for(List<String> row : this.game_board.) {
			if (this.game_board.get(row_placed).get(move).equals("[]")) {
				switch (player.getColour()) {
				case ("Blue"):
					this.game_board.get(row_placed).set(move, player.getPlayerShape());
					break;
				case ("Red"):
					this.game_board.get(row_placed).set(move, player.getPlayerShape());
					break;
				}
				//System.out.println(row);
				move_placed = true;
				break;

			}
			row_placed++;
			
			
		}*/

		for (int i = this.game_board.size()-1; i >= 0; i--) {
			System.out.println("String print");
			System.out.println(this.game_board.get(0).get(move-1));
			System.out.println("end print");
				if (this.game_board.get(i).get(move-1).equals("[]")) {
					switch (player.getColour()) {
					case ("Blue"):
						this.game_board.get(i).set(move-1, player.getPlayerShape());
						break;
					case ("Red"):
						this.game_board.get(i).set(move-1, player.getPlayerShape());
						break;
					}
					row = i;
					System.out.println(row);
					move_placed = true;
					break;

				}
			
		}
		this.checkForWin(row, move, player.getColour(), player.getPlayerShape());
		return move_placed;
	}

	public int getNumOfPlayers() {
		return this.player_list.size();
	}

	public void start_game() {
		this.setUpGameBoard();
	}

	public void checkForWin(int row, int col, String player_colour, String shape) {

		String streak = String.format("%s%s%s%s%s", shape, shape, shape, shape, shape);
		List<String> horizontalLine = game_board.get(row);
		System.out.println("back "+this.getBackSlash(row, col-1).indexOf(streak));
		System.out.println("forward "+this.getForwardSlash(row, col-1).indexOf(streak));
		System.out.println("vert "+this.getVerticalLine(col-1).indexOf(streak));
		
		if (this.getBackSlash(row, col-1).indexOf(streak) >= 0 || this.getVerticalLine(col-1).indexOf(streak) >= 0
				|| this.getForwardSlash(row, col-1).indexOf(streak) >= 0
				|| Collections.frequency(horizontalLine, shape) == 5) {
			System.out.println("win");
			for (List<String> test: game_board) {
				System.out.println(test);
			}
			System.out.println("------------------------------------------");

		}
		
		for (List<String> test: game_board) {
			System.out.println(test);
		}
		System.out.println("------------------------------------------");

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

	static class GameHandler implements HttpHandler {
		private server game;

		public GameHandler(server game) {
			this.game = game;
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
					t.sendResponseHeaders(200, response.length());
					os.write(response.getBytes());
					os.close();
					break;
				case ("get_status"):
					for (player_info player : game.player_list) {
						if (!t.getRequestHeaders().getFirst("player").equals(player.getName())) {
							response = player.getState();
							response += "," + player.getTurn();
						}
					}
					t.sendResponseHeaders(200, response.length());
					os.write(response.getBytes());
					os.close();
					break;

				case ("make_move"):
					try {
						Boolean move_made = false;

						for (player_info player : game.player_list) {
							if (t.getRequestHeaders().getFirst("player").equals(player.getName())) {
								move_made = game.addMove(Integer.parseInt(t.getRequestHeaders().getFirst("move")),
										player);
								player.setTurn(false);
							}
						}
						if(move_made) {
							for (player_info other_player : game.player_list) {
								if(!t.getRequestHeaders().getFirst("player").equals(other_player.getName())) {
									other_player.setTurn(true);
								}
							}
						}

						t.sendResponseHeaders(200, move_made.toString().length());
						os.write(move_made.toString().getBytes());
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
					if (t.getRequestHeaders().getFirst("new").equals("true")) {
						player_info player = game.new player_info(t.getRequestHeaders().getFirst("player"),
								t.getRequestHeaders().getFirst("colour"), t.getRequestHeaders().getFirst("state"),
								false);
						game.player_list.add(player);
						if (player.getColour().equals("Red")) {
							player.setPlayerShape("[X]");
							player.setTurn(true);
						} else
							player.setPlayerShape("[O]");
					}
					String response;
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

	private void checkForWin() {

		this.getGameBoard().get(2).get(4);
		this.getGameBoard().get(1).get(3);
	}

	class player_info {
		private String name;
		private String colour;
		private String state;
		private boolean turn;
		private String player_shape;

		public player_info(String name, String colour, String State, boolean Turn) {
			this.name = name;
			this.colour = colour;
			this.state = State;
			this.turn = Turn;

		}

		private String getName() {
			return name;
		}

		private String getColour() {
			return this.colour;
		}

		private String getState() {
			return this.state;
		}

		private void setState(String state) {
			this.state = state;
		}

		private void setTurn(boolean turn) {
			this.turn = turn;
		}

		private boolean getTurn() {
			return turn;
		}

		private String getPlayerShape() {
			return this.player_shape;
		}

		private void setPlayerShape(String shape) {
			this.player_shape = shape;
		}

	}

}
