package tech_excercise;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Scanner;

public class client {
	private String player_name;
	private String player_colour;
	private Scanner sc;
	private HttpClient httpClient = null;
	private String response="";
	private int move;
	private String status="";
	private StringBuilder currentBoard = new StringBuilder();

	public Scanner getScanner() {
		return this.sc;
	}
	
	public void setScanner(Scanner scanner) {
		this.sc = scanner;
	}
	
	public void setHttpClient(HttpClient client) {
	this.httpClient = client;	
	}
	
	public void setStatus(String new_status) {
		 this.status = new_status;
	}
	
	public StringBuilder getCurrentBoard() {
		return this.currentBoard;
	}
	
	public int getMove() {
		return this.move;
	}

	public String getResponse() {
		return this.response;
	}

	public HttpClient getHttpClient() {
		return this.httpClient;
	}

	public String getPlayerName() {
		return this.player_name;
	}

	public String getPlayerColour() {
		return this.player_colour;
	}

	public void setPlayerNameAndColour(String name, String colour) {
		this.player_colour = colour;

		this.player_name = name;
	}
	
	public boolean collectMove() {
		
		System.out.println("Please Choose a Column");
		this.move = this.sc.nextInt();
		
		System.out.print(this.move);
		if (this.move > 9) {
			System.out.println("Invalid Move");
			return false;
		} else if (this.move == 0) {
			throw new exit_exception("You Have Lost!");
		} else {
			return true;
		}
	}

	void make_move() throws Throwable {
		System.out.print(currentBoard);

		while (!this.collectMove());

		this.response = this.Post("make_move", String.valueOf(this.move));
		if (Boolean.valueOf(response)) {
			System.out.println("You have won!");
			throw new exit_exception("You Have Won");
		}

	}

	public String Post(String msg) throws IOException, InterruptedException {

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:4500/game"))
				.timeout(Duration.ofMinutes(1)).header("Content-Type", "text/plain").POST(BodyPublishers.ofString(msg))
				.setHeader("player", this.player_name).build();

		this.response = this.httpClient.send(request, BodyHandlers.ofString()).body();
		return this.response;
	}

	public String Post(String msg, String move) throws IOException, InterruptedException {


			HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:4500/game"))
					.timeout(Duration.ofMinutes(1)).header("Content-Type", "text/plain")
					.header("player", this.player_name).header("move", move).POST(BodyPublishers.ofString(msg)).build();

			this.response = this.httpClient.send(request, BodyHandlers.ofString()).body();

		

		return this.response;

	}

	public String Get() throws IOException, InterruptedException {

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:4500/game"))
				.timeout(Duration.ofMinutes(1)).header("Content-Type", "text/plain").GET()
				.header("player", this.player_name).header("new", "false").build();

		this.response = this.httpClient.send(request, BodyHandlers.ofString()).body();
		return this.response;
	}

	public String RegisterNewPlayer() throws IOException, InterruptedException {

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:4500/game"))
				.timeout(Duration.ofMinutes(1)).header("Content-Type", "text/plain").GET()
				.header("player", this.player_name).header("new", "true").header("state", "ok")
				.header("colour", this.player_colour).build();

		this.response = this.httpClient.send(request, BodyHandlers.ofString()).body();
		return this.response;
	}

	public void quit_game() throws IOException, InterruptedException {
		String msg = "quit";
		Post(msg);

	}

	public void poll() throws Throwable {
		
	    
		
		
		String[] status = this.status.split("-");
		
		if(status.length ==5) {
		if (status[2].equals("true")) {
			//GetStatus.complete(null);
			throw new exit_exception("You Have Lost");
		} else if (status[0].equals("quit")) {
			System.out.println("Other Player has quit");
			//GetStatus.complete(null);
			throw new exit_exception("You Have Won!");
		} else if (status[1].equals("false")) {
			//GetStatus.complete(null);
			System.out.println("Time to make a move");
			this.setGameBoard(status[4].toString());
			this.make_move();
		}
		}

	}

	public void setGameBoard(String board) throws InterruptedException {
		String cleanBoard = board.substring(0, board.length() - 1);
		StringBuilder Stringboard = new StringBuilder();

		for (String row : cleanBoard.split("\\+")) {
			Stringboard.append(row);
			Stringboard.append("\n");
		}

		this.currentBoard = Stringboard;

	}

	public void gameStartCheck() throws IOException, InterruptedException, Throwable {
		if (this.Get().equals("NR")) {
			System.out.println("Waiting for player");
		} else if (this.Get().equals("R"))
			this.poll();
	}

	public static void main(String[] args) throws Throwable {

		

			client player = new client();
			player.setHttpClient(HttpClient.newHttpClient());
			player.setScanner(new Scanner(System.in));
			System.out.println("Please Enter your Name");
			String name = player.getScanner().next();
			System.out.println("Please select your colour 1: Red 2: Blue");
			int colour = player.getScanner().nextInt();
			String player_colour;
			if (colour == 1) {
				player_colour = "Red";
			} else
				player_colour = "Blue";

			player.setPlayerNameAndColour(name, player_colour);
			player.RegisterNewPlayer();


			Thread printingHook = new Thread(() -> {
				try {
					player.quit_game();

				} catch (IOException | InterruptedException e) {
					
					e.printStackTrace();
				}
			});
			
			Thread get_status = new Thread (() ->{try {
				while(!player.status.contains("quit")) {
				player.status = player.Post("get_status");
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}});
			
			get_status.start();

			Runtime.getRuntime().addShutdownHook(printingHook);
			while(true) {
				player.gameStartCheck();
				Thread.sleep(50);
			}
			

		

	}

}

class exit_exception extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public exit_exception(String msg) {
		System.out.print(msg);

	}

}
