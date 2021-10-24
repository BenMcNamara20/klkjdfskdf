package tech_excercise;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

public class client_thread implements Runnable {

	private client player;
	public client_thread(client player) {
		this.player = player;
	}
	@Override
	public void run() {
		System.out.println("client thread");
		sendMessage();
		
	}
	
	public void sendMessage() {
		try {
		BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
		DataOutputStream output = null;
		output = new DataOutputStream(player.socket.getOutputStream());
		 DataInputStream intput_stream = new DataInputStream(player.socket.getInputStream());

		String line_input = "";
		String server_response = "";
		while(!line_input.equals("x")) {
		line_input = input.readLine();
		output.writeUTF(line_input);
		server_response = intput_stream.readUTF();
		System.out.println(server_response);
		}
		System.out.println("Thanks for playing");
		player.socket.close();
		}
		catch(Exception e) {
			System.out.println(e);
		}

}
}
