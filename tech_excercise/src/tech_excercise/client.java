package tech_excercise;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;

public class client {
	private String player_name;
	private String player_colour;
	private Scanner sc = new Scanner(System.in);
	private HttpClient httpClient = null;

	public client(HttpClient client) {
		this.httpClient = client;
	}

	public void setPlayerNameAndColour() {

		System.out.print("Please enter your name ");
		this.player_name = this.sc.next();
		System.out.println("Please select a colour 1: Red 2:Blue");
		boolean colour_selected = false;
		while (!colour_selected) {
			int colour_select = this.sc.nextInt();
			switch (colour_select) {
			case (1):
				this.player_colour = "Red";
				colour_selected = true;
				break;
			case (2):
				this.player_colour = "Blue";
				colour_selected = true;
				break;
			default:
				System.out.println("Invalid Selection, Please select again");
			}
		}
	}

	void make_move() throws IOException, InterruptedException {
		boolean valid_move = false;
		int move = 0;
		while (!valid_move) {
			System.out.println("Please make a move by entering a number between 1 and 9 or enter 10 to quit");
			move = this.sc.nextInt();
			if (move < 1 || move > 9) {
				System.out.println("Invalid Move");
			} else if (move == 10) {
				this.quit_game();
			} else {
				valid_move = true;
			}
		}
		String response = this.Post("make_move", String.valueOf(move));
		System.out.println(response);

	}

	public String Post(String msg) throws IOException, InterruptedException {

		String result = "";
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:4500/game"))
				.timeout(Duration.ofMinutes(1)).header("Content-Type", "text/plain").POST(BodyPublishers.ofString(msg))
				.setHeader("player", this.player_name).build();

		result = this.httpClient.send(request, BodyHandlers.ofString()).body();
		return result;
	}

	public String Post(String msg, String move) throws IOException, InterruptedException {

		String result = "";
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:4500/game"))
				.timeout(Duration.ofMinutes(1)).header("Content-Type", "text/plain").header("player", this.player_name)
				.header("move", move).POST(BodyPublishers.ofString(msg)).build();

		result = this.httpClient.send(request, BodyHandlers.ofString()).body();
		return result;
	}

	public String Get() throws IOException, InterruptedException {

		String result = "";
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:4500/game"))
				.timeout(Duration.ofMinutes(1)).header("Content-Type", "text/plain").GET()
				.header("player", this.player_name).header("new", "false").build();

		result = this.httpClient.send(request, BodyHandlers.ofString()).body();
		return result;
	}

	public String RegisterNewPlayer(String new_player) throws IOException, InterruptedException {

		String result = "";
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:4500/game"))
				.timeout(Duration.ofMinutes(1)).header("Content-Type", "text/plain").GET()
				.header("player", this.player_name).header("new", "true").header("state", "ok")
				.header("colour", this.player_colour).build();

		result = this.httpClient.send(request, BodyHandlers.ofString()).body();
		return result;
	}

	public void quit_game() throws IOException, InterruptedException {
		String msg = "quit";
		Post(msg);
		System.exit(0);
	}

	public void poll() throws IOException, Exception {
		String response = "";
		String msg = "get_status";

		response = Post(msg);

		while (response.split(",")[0].equals("ok") && response.split(",")[1].equals("true")) {

			response = Post(msg);

		}
		if (response.split(",")[0].equals("not_ok")) {
			this.quit_game();
		} else if (response.split(",")[1].equals("false")) {
			System.out.println("Time to make a move");
			this.make_move();
		}

	}

	public String decodeMsg(HttpURLConnection con) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String inputLine = "";
		inputLine = in.readLine();
		in.close();
		return inputLine;

	}
	
	public void checkForWinTest() {
		List<List<String>> board = new ArrayList<List<String>>();
		for (int x = 0; x < 9; x++) {
			board.add(Arrays.asList("  []  ", "  []  ", "  []  ", "  []  ", "  []  ", "  []"));
		}
		for(List<String> col : board) {
			for(String cell : col) {
			System.out.print(cell);
			}
			System.out.println();
			
			
		}
		
		
	}
	
	public void checkForWin() {
		int move = 2;
		int row = 2;
		List<List<String>> game_board = Collections.synchronizedList(new ArrayList<>());
		game_board.add(Arrays.asList("[X]", "[]", "[]", "[]", "[]", "[]", "[]", "[]", "[]"));
		game_board.add(Arrays.asList("[]", "[X]", "[]", "[]", "[]", "[]", "[]", "[]", "[]"));
		game_board.add(Arrays.asList("[]", "[]", "[X]", "[]", "[]", "[]", "[]", "[]", "[]"));
		
		
		
		List<String> horizontalLine = game_board.get(row);
        /*StringBuilder verticalLine = new StringBuilder(9);		
		for(int i = 0; i < 3; i++) {
			game_board.get(i).get(move);
			verticalLine.append(game_board.get(i).get(move));
		}
		
		System.out.println(verticalLine);*/
		
		/*StringBuilder forwardSlash = new StringBuilder();
		
		for(int h = 0; h < 3;h++) {
			int w = move + row - h;
			if(0 <= w && w < 9) {
				forwardSlash.append(game_board.get(h).get(w));
			}
		}*/
		//System.out.println(forwardSlash);
		
		StringBuilder backSlash = new StringBuilder();
		
		
		for(int h = 0; h < 3;h++) {
			int w = move - row + h;
			if(0 <= w && w < 9) {
				backSlash.append(game_board.get(h).get(w));
			}
		}
		String sym = "[X]";
		String streak = String.format("%s%s%s%s",  sym,sym, sym, sym);
		System.out.println(backSlash.indexOf(streak));
		
		System.out.println(Collections.frequency(horizontalLine, "[X]"));
	}

	public static void main(String[] args) throws Exception {
		Thread main_game_thread = new Thread() {
			public void run() {
				try {
					client player = new client(HttpClient.newHttpClient());
					//player.checkForWin();
					player.setPlayerNameAndColour();
					player.RegisterNewPlayer("true");
					while (true) {
						if (player.Get().equals("NR")) {
							System.out.println("Waiting for player");
						} else if (player.Get().equals("R"))
							player.poll();

					}
				} catch (Exception e) {
					System.out.print(e);
				}
			}
		};
		main_game_thread.start();

	}

}