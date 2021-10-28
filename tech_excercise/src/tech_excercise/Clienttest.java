package tech_excercise;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Scanner;
import java.util.concurrent.ScheduledFuture;

import javax.swing.JFrame;

import org.junit.Rule;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;
import org.powermock.core.PowerMockUtils;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.*;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
@RunWith(PowerMockRunner.class)
@PrepareForTest({client.class})
class Clienttest {

	@Mock
	HttpClient MockClient;
	
	Scanner sc;

	client player;

	@BeforeAll
	static void setUpBeforeClass() throws Exception {

	}

	@AfterAll
	static void tearDownAfterClass() throws Exception {
	}

	@BeforeEach
	void setUp() throws Exception {
		player = new client();
		player.setHttpClient(MockClient);
		player.setScanner(sc);

	}

	@AfterEach
	void tearDown() throws Exception {
	}

	@Test
	void testClient() throws IOException, InterruptedException {

		assertEquals(player.getHttpClient(), MockClient);
		assertEquals(player.getScanner(), sc);
	}

	@Test
	void testSetPlayerNameAndColour() {
		String name = "ben";
		String colour = "Blue";
		player.setPlayerNameAndColour(name, colour);
		assertEquals(player.getPlayerColour(), colour);
		assertEquals(player.getPlayerName(), name);
	}

	@Rule
	public ExpectedException exceptionRule;

	@Test
	void testMake_move() throws Throwable {
		HttpClient MockClient = mock(HttpClient.class, Mockito.CALLS_REAL_METHODS);
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:4500/game"))
				.timeout(Duration.ofMinutes(1)).header("Content-Type", "text/plain").header("player", "ben")
				.header("move", "1").POST(BodyPublishers.ofString("make_move")).build();

		HttpResponse<String> MockResponse = mock(HttpResponse.class, Mockito.CALLS_REAL_METHODS);
		when(MockClient.send(request, BodyHandlers.ofString())).thenReturn(MockResponse);
		when(MockResponse.body()).thenReturn("test");

		String name = "ben";
		String colour = "Blue";
		client new_player = new client();
		new_player.setHttpClient(MockClient);
		new_player.setScanner(new Scanner("1"));
		
		new_player.setPlayerNameAndColour(name, colour);
		new_player.make_move();
		assertEquals("test", new_player.getResponse());
		
		client win_player = new client();
		win_player.setHttpClient(MockClient);
		win_player.setScanner(new Scanner("1"));
		win_player.setPlayerNameAndColour(name, colour);
		

		when(MockResponse.body()).thenReturn("true");
		
		assertThrows(exit_exception.class, () -> {
			win_player.make_move();
		});
		
		client invalid_move_player = new client();
		invalid_move_player.setHttpClient(MockClient);
		invalid_move_player.setScanner(new Scanner("10"));
		invalid_move_player.setPlayerNameAndColour(name, colour);
		

		when(MockResponse.body()).thenReturn("false");
        invalid_move_player.collectMove();
		assertEquals(invalid_move_player.getMove(),10);
		
		client quit_move_player = new client();
		quit_move_player.setHttpClient(MockClient);
		quit_move_player.setScanner(new Scanner("0"));
		
		
		
		invalid_move_player.setPlayerNameAndColour(name, colour);
		

		when(MockResponse.body()).thenReturn("false");
		
		assertThrows(exit_exception.class, () -> {
			quit_move_player.collectMove();
		});
		
		
		
		
		
		

	}

	@Test
	void testPostString() throws IOException, InterruptedException {
		HttpClient MockClient = mock(HttpClient.class, Mockito.CALLS_REAL_METHODS);
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:4500/game"))
				.timeout(Duration.ofMinutes(1)).header("Content-Type", "text/plain").header("player", "ben")
				.POST(BodyPublishers.ofString("make_move")).build();

		HttpResponse<String> MockResponse = mock(HttpResponse.class, Mockito.CALLS_REAL_METHODS);

		when(MockClient.send(request, BodyHandlers.ofString())).thenReturn(MockResponse);
		when(MockResponse.body()).thenReturn("test");

		player = new client();
		player.setHttpClient(MockClient);
		player.setScanner(new Scanner(System.in));

		String name = "ben";
		String colour = "Blue";
		player.setPlayerNameAndColour(name, colour);

		player.Post("test");
		assertEquals("test", player.getResponse());
		
		

	}
	
	
	@Test
	void testQuitGame() throws IOException, InterruptedException {
		HttpClient MockClient = mock(HttpClient.class, Mockito.CALLS_REAL_METHODS);
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:4500/game"))
				.timeout(Duration.ofMinutes(1)).header("Content-Type", "text/plain").header("player", "ben")
				.POST(BodyPublishers.ofString("quit")).build();

		HttpResponse<String> MockResponse = mock(HttpResponse.class, Mockito.CALLS_REAL_METHODS);

		when(MockClient.send(request, BodyHandlers.ofString())).thenReturn(MockResponse);
		when(MockResponse.body()).thenReturn("test");

		player.setHttpClient(MockClient);
		player.setScanner(new Scanner(System.in));

		String name = "ben";
		String colour = "Blue";
		player.setPlayerNameAndColour(name, colour);

		player.quit_game();
		assertEquals(player.getResponse(),"test");
	}

	@Test
	void testPostStringString() throws IOException, InterruptedException {
		HttpClient MockClient = mock(HttpClient.class, Mockito.CALLS_REAL_METHODS);

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:4500/game"))
				.timeout(Duration.ofMinutes(1)).header("Content-Type", "text/plain").header("player", "ben")
				.header("move", "1").POST(BodyPublishers.ofString("make_move")).build();

		HttpResponse<String> MockResponse = mock(HttpResponse.class, Mockito.CALLS_REAL_METHODS);

		when(MockClient.send(request, BodyHandlers.ofString())).thenReturn(MockResponse);
		when(MockResponse.body()).thenReturn("test");

		player.setHttpClient(MockClient);
		player.setScanner(new Scanner(System.in));

		String name = "ben";
		String colour = "Blue";
		player.setPlayerNameAndColour(name, colour);

		player.Post("make_move", "1");
		assertEquals("test", player.getResponse());
		
		
		
		when(MockResponse.body()).thenThrow(RuntimeException.class);
		
		
		assertThrows(RuntimeException.class, () -> {
			player.Post("test");
		});
		
	}

	@Test
	void testGet() throws IOException, InterruptedException {
		HttpClient MockClient = mock(HttpClient.class, Mockito.CALLS_REAL_METHODS);

		player.setHttpClient(MockClient);
		player.setScanner(new Scanner(System.in));

		String name = "ben";
		String colour = "Blue";
		player.setPlayerNameAndColour(name, colour);

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:4500/game"))
				.timeout(Duration.ofMinutes(1)).header("Content-Type", "text/plain").GET()
				.header("player", player.getPlayerName()).header("new", "false").build();

		HttpResponse<String> MockResponse = mock(HttpResponse.class, Mockito.CALLS_REAL_METHODS);

		when(MockClient.send(request, BodyHandlers.ofString())).thenReturn(MockResponse);
		when(MockResponse.body()).thenReturn("test");
		player.Get();

		assertEquals("test", player.getResponse());
	}

	@Test
	void testRegisterNewPlayer() throws IOException, InterruptedException {
		HttpClient MockClient = mock(HttpClient.class, Mockito.CALLS_REAL_METHODS);

		player.setHttpClient(MockClient);
		player.setScanner(new Scanner(System.in));

		String name = "ben";
		String colour = "Blue";
		player.setPlayerNameAndColour(name, colour);

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:4500/game"))
				.timeout(Duration.ofMinutes(1)).header("Content-Type", "text/plain").GET()
				.header("player", player.getPlayerName()).header("new", "true").header("state", "ok")
				.header("colour", player.getPlayerColour()).build();

		HttpResponse<String> MockResponse = mock(HttpResponse.class, Mockito.CALLS_REAL_METHODS);

		when(MockClient.send(request, BodyHandlers.ofString())).thenReturn(MockResponse);
		when(MockResponse.body()).thenReturn("test");
		player.RegisterNewPlayer();
		assertEquals("test", player.getResponse());

	}

	@Test
	void testQuit_game() throws IOException, InterruptedException {

		HttpClient MockClient = mock(HttpClient.class, Mockito.CALLS_REAL_METHODS);
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:4500/game"))
				.timeout(Duration.ofMinutes(1)).header("Content-Type", "text/plain").header("player", "ben")
				.POST(BodyPublishers.ofString("quit")).build();

		HttpResponse<String> MockResponse = mock(HttpResponse.class, Mockito.CALLS_REAL_METHODS);

		when(MockClient.send(request, BodyHandlers.ofString())).thenReturn(MockResponse);
		when(MockResponse.body()).thenReturn("test");

		player.setHttpClient(MockClient);
		player.setScanner(new Scanner(System.in));

		String name = "ben";
		String colour = "Blue";
		player.setPlayerNameAndColour(name, colour);

		player.Post("test");
		assertEquals("test", player.getResponse());

	}

	@Test
	void testPoll() throws Throwable {
		HttpClient MockClient = mock(HttpClient.class, Mockito.CALLS_REAL_METHODS);

		player.setHttpClient(MockClient);
		player.setScanner(new Scanner("1"));

		String name = "ben";
		String colour = "Blue";
		player.setPlayerNameAndColour(name, colour);

		
		player.setStatus("quit-true-false-1-[]+[]+[]+");

		assertThrows(exit_exception.class, () -> {
			player.poll();
		});

		player.setStatus("ok-false-false-1-[]+[]+[]+");

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:4500/game"))
				.timeout(Duration.ofMinutes(1)).header("Content-Type", "text/plain")
				.header("player", player.getPlayerName()).header("move", "1").POST(BodyPublishers.ofString("make_move")).build();

		HttpResponse<String> MockResponse = mock(HttpResponse.class, Mockito.CALLS_REAL_METHODS);

		when(MockClient.send(request, BodyHandlers.ofString())).thenReturn(MockResponse);
		when(MockResponse.body()).thenReturn("true");

		assertThrows(exit_exception.class, () -> {
			player.poll();
		});
		
		
		player.setStatus("ok-false-true-1-[]+[]+[]+");

		assertThrows(exit_exception.class, () -> {
			player.poll();
		});
		

		
		client no_move = new client();
		no_move.setHttpClient(MockClient);
		no_move.setScanner(new Scanner("1"));
		
		no_move.setPlayerNameAndColour("ben", "blue");
		no_move.poll();

		assertEquals(no_move.getMove(),0);


	}

	@Test
	void testPrintGameBoard() throws InterruptedException {
		HttpClient MockClient = mock(HttpClient.class, Mockito.CALLS_REAL_METHODS);

		player.setHttpClient(MockClient);
		player.setScanner(new Scanner(System.in));

		String name = "ben";
		String colour = "Blue";
		player.setPlayerNameAndColour(name, colour);
		String testBoard = "1+1+1+";
		player.setGameBoard(testBoard);

		StringBuilder expected = new StringBuilder();
		String board = "1+1+1+";
		String cleanBoard = board.substring(0, board.length() - 1);

		for (String row : cleanBoard.split("\\+")) {
			expected.append(row);
			expected.append("\n");
		}

		assertEquals(player.getCurrentBoard().toString(), expected.toString());

	}
	
	@Test
	void testGameStartCheck() throws Throwable {
		HttpClient MockClient = mock(HttpClient.class, Mockito.CALLS_REAL_METHODS);

		player.setHttpClient(MockClient);
		player.setScanner(new Scanner(System.in));

		String name = "ben";
		String colour = "Blue";
		player.setPlayerNameAndColour(name, colour);

		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("http://localhost:4500/game"))
				.timeout(Duration.ofMinutes(1)).header("Content-Type", "text/plain").GET()
				.header("player", player.getPlayerName()).header("new", "false").build();

		HttpResponse<String> MockResponse = mock(HttpResponse.class, Mockito.CALLS_REAL_METHODS);

		when(MockClient.send(request, BodyHandlers.ofString())).thenReturn(MockResponse);
		when(MockResponse.body()).thenReturn("NR");
		player.gameStartCheck();

		assertEquals("NR", player.getResponse());
		
		when(MockResponse.body()).thenReturn("R");
		
		player.setStatus("ok-true-true-1-[]+[]+[]+");

		assertThrows(exit_exception.class, () -> {
			player.gameStartCheck();
		});
		

		
		when(MockResponse.body()).thenReturn("X");
		client InvalidPoll = new client();
		InvalidPoll.setHttpClient(MockClient);
		InvalidPoll.setScanner(new Scanner(System.in));
		InvalidPoll.setPlayerNameAndColour("ben", "blue");
		InvalidPoll.gameStartCheck();

		assertEquals(InvalidPoll.getResponse(),"X");
		
	}

}
