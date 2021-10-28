package tech_excercise;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import tech_excercise.server.GameHandler;

class serverTest {
	
	server testServer;
	GameHandler gameTest;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		
	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
		testServer = new server();
		gameTest = new GameHandler(testServer);
	}

	@AfterEach
	void tearDown() throws Exception {
		gameTest =  null;
		testServer =null;
	}

	@Test
	void setupGameBoardTest() {
		server game = new server();
		game.setUpGameBoard();
		assertEquals(game.getGameBoard().size(),6);
	}
	
	@Test
	void addMoveTest() {
		
		player_info test = new player_info("ben", "Blue", "ok", true);
		testServer.setUpGameBoard();
		test.setPlayerShape("X");
		testServer.addMove(1, test);
		System.out.println(testServer.getGameBoard());
		assertEquals(testServer.getGameBoard().get(testServer.getGameBoard().size()-1).get(0),"X");
		
		player_info RedPlayer = new player_info("ben", "Red", "ok", true);
		RedPlayer.setPlayerShape("O");
		testServer.addMove(2, RedPlayer);
		assertEquals(testServer.getGameBoard().get(testServer.getGameBoard().size()-1).get(1),"O");
		
		
		RedPlayer.setPlayerShape("O");
		testServer.addMove(1, RedPlayer);
		assertEquals(testServer.getGameBoard().get(testServer.getGameBoard().size()-2).get(0),"O");
	
	}
	
	@Test
	void getNumPlayersTest() {
		player_info RedPlayer = new player_info("ben", "Red", "ok", true);
		testServer.getPlayerList().add(RedPlayer);
		assertEquals(testServer.getNumOfPlayers(),1);
		
	}
	
	@Test
	void checkForWinTest() {
		testServer.setUpGameBoard();
		List<String> forward_row;
		for(int i =1;i<6;i++) {
		forward_row=testServer.getGameBoard().get(testServer.getGameBoard().size()-i);
		forward_row.set(i, "X");
		}

		assertTrue(testServer.checkForWin(6, 1, "Blue", "X"));
		System.out.println();
		List<String> backrow;
		testServer.setUpGameBoard();
		for(int i =1;i<6;i++) {
		backrow=testServer.getGameBoard().get(testServer.getGameBoard().size()-i);
		backrow.set(8-i, "X");
		}

		assertTrue(testServer.checkForWin(5, 8, "Blue", "X"));
		
		
	}
	
	@Test
	void testHandlerPOST() throws IOException {
		
		player_info RedPlayer = new player_info("ben", "Red", "ok", true);
		testServer.getPlayerList().add(RedPlayer);
		
		//GameHandler gameTest = new GameHandler(testServer);
		HttpExchange exchange = mock(HttpExchange.class);
		Headers  httpHeaders = new Headers();
		List<String> Namelist = new ArrayList<String>();
		Namelist.add("ben");
		
		List<String> moveList = new ArrayList<String>();
		moveList.add("1");
		
		httpHeaders.putIfAbsent("player", Namelist);
		httpHeaders.putIfAbsent("move", moveList);
		when(exchange.getRequestHeaders()).thenReturn(httpHeaders);
		when(exchange.getRequestMethod()).thenReturn("POST");
		String quit = "quit";
		InputStream in = new ByteArrayInputStream(quit.getBytes());
		when(exchange.getRequestBody()).thenReturn(in);
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		
		when(exchange.getResponseBody()).thenReturn(os);
		gameTest.handle(exchange);
		assertEquals(RedPlayer.getState(),"quit");
		
		String status = "get_status";
		in = new ByteArrayInputStream(status.getBytes());
		player_info BluePlayer = new player_info("joe", "Blue", "ok", true);
		testServer.getPlayerList().add(BluePlayer);
		testServer.setUpGameBoard();
		
		when(exchange.getRequestBody()).thenReturn(in);
		
		gameTest.handle(exchange);
		
		System.out.print(os.toString());
		String response =  "ok-true-false-0-[, , , , , , , , ]+[, , , , , , , , ]+[, , , , , , , , ]+[, , , , , , , , ]+[, , , , , , , , ]+[, , , , , , , , ]+";
		assertEquals(os.toString(),response);
		
		String make_move = "make_move";
		in = new ByteArrayInputStream(make_move.getBytes());

		when(exchange.getRequestBody()).thenReturn(in);
		RedPlayer.setPlayerShape("X");
		gameTest.handle(exchange);
		assertEquals(RedPlayer.getLastMove(),1);
		assertEquals(RedPlayer.getHasWon(),false);
		assertEquals(RedPlayer.getTurn(),false);
		
	}
	
	@Test
	void testHandlerGET() throws IOException {
		
		
		//GameHandler gameTest = new GameHandler(testServer);
		HttpExchange exchange = mock(HttpExchange.class);
		Headers  httpHeaders = new Headers();
		
		List<String> Namelist = new ArrayList<String>();
		Namelist.add("ben");
		
		List<String> newList = new ArrayList<String>();
		newList.add("true");
		
		List<String> colourList = new ArrayList<String>();
		colourList.add("Red");
		
		List<String> stateList = new ArrayList<String>();
		stateList.add("ok");
		
		httpHeaders.putIfAbsent("player", Namelist);
		httpHeaders.putIfAbsent("new", newList);
		httpHeaders.putIfAbsent("colour", colourList);
		httpHeaders.putIfAbsent("state", stateList);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		when(exchange.getResponseBody()).thenReturn(os);
		
		
		when(exchange.getRequestHeaders()).thenReturn(httpHeaders);
		when(exchange.getRequestMethod()).thenReturn("GET");

		gameTest.handle(exchange);
		assertEquals(testServer.getPlayerList().get(0).getPlayerShape(),"X");
		assertEquals(testServer.getPlayerList().get(0).getName(),"ben");
		assertEquals(testServer.getPlayerList().get(0).getState(),"ok");
		assertEquals(testServer.getPlayerList().get(0).getColour(),"Red");
		assertEquals(os.toString(),"NR");
		os.reset();

		
		
		
		
	}
	
	@Test
	void testGameready() throws IOException {
		
		player_info RedPlayer = new player_info("ben", "Red", "ok", true);
		testServer.getPlayerList().add(RedPlayer);
		//GameHandler gameTest = new GameHandler(testServer);
		HttpExchange exchange = mock(HttpExchange.class);
		Headers  httpHeaders = new Headers();
		
		List<String> Namelist = new ArrayList<String>();
		Namelist.add("ben");
		
		List<String> newList = new ArrayList<String>();
		newList.add("true");
		
		List<String> colourList = new ArrayList<String>();
		colourList.add("Red");
		
		List<String> stateList = new ArrayList<String>();
		stateList.add("ok");
		
		httpHeaders.putIfAbsent("player", Namelist);
		httpHeaders.putIfAbsent("new", newList);
		httpHeaders.putIfAbsent("colour", colourList);
		httpHeaders.putIfAbsent("state", stateList);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		when(exchange.getResponseBody()).thenReturn(os);
		
		when(exchange.getRequestHeaders()).thenReturn(httpHeaders);
		when(exchange.getRequestMethod()).thenReturn("GET");
		gameTest.handle(exchange);
		assertEquals(os.toString(),"R");
		assertTrue((testServer.getGameBoard().size()>0));
		
	}
	
	

}
