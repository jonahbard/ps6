import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;

/**
 * Handles communication between the server and one client, for SketchServer
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 */
public class SketchServerCommunicator extends Thread {
	private Socket sock;					// to talk with client
	private BufferedReader in;				// from client (editor communicator [out])
	private PrintWriter out;				// to client  (editor communicator [in])
	private SketchServer server;			// handling communication for

	public SketchServerCommunicator(Socket sock, SketchServer server) {
		this.sock = sock;
		this.server = server;
	}

	/**
	 * Sends a message to the client
	 * @param msg
	 */
	public void send(String msg) {
		out.println(msg);
	}
	
	/**
	 * Keeps listening for and handling (your code) messages from the client
	 */
	public void run() {
		try {
			System.out.println("someone connected");
			
			// Communication channel
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			out = new PrintWriter(sock.getOutputStream(), true);

			// Tell the client the current state of the world
			// TODO: YOUR CODE HERE
			String line;
			while ((line = in.readLine()) != null){
				handleMessageFromEditor(line);
			}
			out.println(server.getSketch());

			// Keep getting and handling messages from the client
			// TODO: YOUR CODE HERE

			// Clean up -- note that also remove self from server's list so it doesn't broadcast here
			server.removeCommunicator(this);
			out.close();
			in.close();
			sock.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void handleMessageFromEditor(String msg){
		String[] commands = msg.split(" ");
		if (commands[0].equals("DRAW")) {
			System.out.println("commands: " + Arrays.toString(commands));
			System.out.println("command len: " + commands.length);
			System.out.println();

			// Format: Command ID shape x1 y1 x2 y2 ColorInt
			// Example Draw msg: DRAW 000001 ellipse 1 2 600 600 -12332

			String[] shapeParams = Arrays.copyOfRange(commands, 2, commands.length);

			String shapeType = shapeParams[0];
			int id = Integer.parseInt(commands[1]);
			int x1 = Integer.parseInt(shapeParams[1]);
			int y1 = Integer.parseInt(shapeParams[2]);
			int x2 = Integer.parseInt(shapeParams[3]);
			int y2 = Integer.parseInt(shapeParams[4]);
			Color color = new Color(Integer.parseInt(shapeParams[5]));

			Shape shape = null;
			if (shapeType.equals("ellipse")) {
				shape = new Ellipse(x1, y1, x2, y2, color);
			} else if (shapeType.equals("rectangle")){
				shape = new Rectangle(x1, y1, x2, y2, color);
			} else if (shapeType.equals("segment")){
				shape = new Segment(x1, y1, x2, y2, color);
			} else if (shapeType.equals("polyline")){
				shape = new Polyline(x1, y1, color);
				//fill in the rest of the polyline here
			}

			if (shape != null) server.getSketch().addShape(id, shape);
		}

		else if (commands[0].equals("MOVE")){ 		// MOVE ID OX OY NX NY

			int id = Integer.parseInt(commands[0]);
			int dx = Integer.parseInt(commands[2]);
			int dy = Integer.parseInt(commands[3]);

			server.moveShape(id, dx, dy);


		} else if (commands[0].equals("REPAINT")){ // REPAINT ID NEWCOLOR
			server.recolorShape(Integer.parseInt(commands[1]), new Color (Integer.parseInt(commands[2])));


		} else if (commands[0].equals("DELETE")) { 		// DELETE ID
			server.removeShape(Integer.parseInt(commands[1]));
		}
		server.broadcast(msg);
	}
}
