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

			out.println("SKETCH " + server.getSketch());

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

        switch (commands[0]) {
            case "DRAW" -> {
                // Format: Command shape x1 y1 x2 y2 ColorInt (if it's a polyline there will be x3 y3, etc)
                // Example Draw msg: DRAW ellipse 1 2 600 600 -12332

                String[] shapeParams = Arrays.copyOfRange(commands, 1, commands.length);
                int id = server.getNextIDAndIncrement();

                Shape shape = MessageParser.parseShape(shapeParams);

                if (shape != null) {
                    server.addShape(id, shape);
                    msg = "DRAW " + id + " " + shape;
                }
            }
            case "MOVE" -> {        // MOVE ID NX NY
                int id = Integer.parseInt(commands[1]);
                int dx = Integer.parseInt(commands[2]);
                int dy = Integer.parseInt(commands[3]);

                server.moveShape(id, dx, dy);

            }
            case "REPAINT" -> { // REPAINT ID NEWCOLOR
                int id = Integer.parseInt(commands[1]);
                Color color = new Color(Integer.parseInt(commands[2]));

                server.recolorShape(id, color);

            }
            case "DELETE" ->  { // DELETE ID
				int id = Integer.parseInt(commands[1]);
				server.removeShape(id);

			}

        }

		server.broadcast(msg);
	}
}
