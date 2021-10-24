package tech_excercise;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class server_thread implements Runnable {
	server game_server;
	
	public server_thread(server game_server) {
		this.game_server = game_server;
	}
	
	public void run() {
		try {
			DataInputStream  in = null;
			BufferedReader buffer = null;
			BufferedWriter write_buffer = null;
			DataOutputStream out = null;

			in = new DataInputStream(game_server.get_socket().getInputStream());

			out = new DataOutputStream(game_server.get_socket().getOutputStream());
			System.out.println("TEST1");
			String msg = "";
			while(!game_server.game_is_active()) {
				System.out.println("Waiting for player");
			}
			
			System.out.println("Player Connected");
			
			while(!msg.equals("x") && game_server.game_is_active()) {
				msg = in.readUTF();
				out.writeUTF("test");
				System.out.println(msg);
			}
			game_server.get_players().remove(Thread.currentThread());
			System.out.println("game over");
		}
		catch(Exception e) {
			System.out.println("");
		}
	}

}
