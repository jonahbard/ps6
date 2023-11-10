import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;

/**
 * Handles communication to/from the server for the editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author Chris Bailey-Kellogg; overall structure substantially revised Winter 2014
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 */
public class EditorCommunicator extends Thread {
	private PrintWriter out;		// to server
	private BufferedReader in;		// from server
	protected Editor editor;		// handling communication for

	/**
	 * Establishes connection and in/out pair
	 */
	public EditorCommunicator(String serverIP, Editor editor) {
		this.editor = editor;
		System.out.println("connecting to " + serverIP + "...");
		try {
			Socket sock = new Socket(serverIP, 4242);
			out = new PrintWriter(sock.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			System.out.println("...connected");
		}
		catch (IOException e) {
			System.err.println("couldn't connect");
			System.exit(-1);
		}
	}

	/**
	 * Sends message to the server
	 */
	public void send(String msg) {
		out.println(msg);
	}

	/**
	 * Keeps listening for and handling (your code) messages from the server
	 */
	public void run() {
		try {
			String line;
			while ((line = in.readLine()) != null) {
				handleMessageFromServer(line);
			}

			// while not sending data TO the server, handle messages FROM the server
			//should constantly set the "sketch" in Editor to be the SketchServer's sketch
			// Handle messages
			// TODO: YOUR CODE HERE
//			throw new IOException("temp");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("server hung up");
		}
	}

	public void handleMessageFromServer(String message) {
		System.out.println(message);
		String[] commands = message.split(" ");
		if (commands[0].equals("DRAW")) {
			System.out.println("commands: " + Arrays.toString(commands));
			System.out.println("command len: " + commands.length);
			System.out.println();

			// Format: Command ID shape x1 y1 x2 y2 ColorInt
			// Example Draw msg: DRAW 000001 ellipse 1 2 600 600 -12332

			boolean hasID = commands.length == 8;

			String[] shapeParams = Arrays.copyOfRange(commands, hasID ? 2 : 1, commands.length);

			String shapeType = shapeParams[0];
			int id = hasID ? Integer.parseInt(commands[1]) : (int) (Math.random() * 1000);
			int x1 = Integer.parseInt(shapeParams[1]);
			int y1 = Integer.parseInt(shapeParams[2]);
			int x2 = Integer.parseInt(shapeParams[3]);
			int y2 = Integer.parseInt(shapeParams[4]);
			Color color = new Color(Integer.parseInt(shapeParams[5]));

			Shape shape = null;
			if (shapeType.equals("ellipse")) {
				shape = new Ellipse(x1, y1, x2, y2, color);
			}

			if (shape != null) editor.getSketch().addShape(id, shape);
			editor.clearCurrentShape();



		}
		// MOVE ID OX OY NX NY
		else if (commands[0].equals("MOVE")){
			int id = Integer.parseInt(commands[0]);
			int ox = Integer.parseInt(commands[2]);
			int oy = Integer.parseInt(commands[3]);
			int nx = Integer.parseInt(commands[4]);
			int ny = Integer.parseInt(commands[5]);

			editor.moveShape(id, ox, oy, nx, ny);
		}
		// REPAINT ID NEWCOLOR
		else if (commands[0].equals("REPAINT")){

		}
		// DELETE ID
		else if (commands[0].equals("DELETE")) {

		}

			editor.callRepaint();
	}

	// Send editor requests to the server
	// TODO: YOUR CODE HERE; CREATE FUNCTIONS FOR EACH TYPE OF MESSAGE TO SEND

	public void sendDrawMessageToServer(Shape shape) {
		out.println("DRAW " + shape.toString());
	}

	public void sendMoveCommand(int id, int ox, int oy, int nx, int ny) {
		out.println("MOVE " + id + " " + ox + " " + oy + " " + nx + " " + ny + " ");
	}

	public void sendRepaintCommand(int id, int newColor) {
		out.println("REPAINT " + id + " " + newColor);
	}
	
}
