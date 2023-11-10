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
			int id = hasID ? Integer.parseInt(commands[1]) : (int) (Math.random() * 1000);

			String[] shapeParams = Arrays.copyOfRange(commands, hasID ? 2 : 1, commands.length);

			String shapeType = shapeParams[0];

			Shape shape = null;
			if (shapeType.equals("polyline")) {

			}
			else {
				int x1 = Integer.parseInt(shapeParams[1]);
				int y1 = Integer.parseInt(shapeParams[2]);
				int x2 = Integer.parseInt(shapeParams[3]);
				int y2 = Integer.parseInt(shapeParams[4]);
				Color color = new Color(Integer.parseInt(shapeParams[5]));

				if (shapeType.equals("ellipse")) shape = new Ellipse(x1, y1, x2, y2, color);
				else if (shapeType.equals("rectangle")) shape = new Rectangle(x1, y1, x2, y2, color);
				else if (shapeType.equals("segment")) shape = new Segment(x1, y1, x2, y2, color);
			}

			if (shape != null) editor.getSketch().addShape(id, shape);
			editor.clearCurrentShape();



		}
		// MOVE ID OX OY NX NY
		else if (commands[0].equals("MOVE")){
			int id = Integer.parseInt(commands[1]);
			int dx = Integer.parseInt(commands[2]);
			int dy = Integer.parseInt(commands[3]);

			editor.moveShape(id, dx, dy);
		}
		// RECOLOR ID NEWCOLOR
		else if (commands[0].equals("RECOLOR")){
			int id = Integer.parseInt(commands[1]);
			Color color = new Color(Integer.parseInt(commands[2]));

			editor.recolorShape(id, color);

		}
		// DELETE ID
		else if (commands[0].equals("DELETE")) {
			int id = Integer.parseInt(commands[1]);

			editor.deleteShape(id);
		}

		editor.callRepaint();
	}

	// Send editor requests to the server
	public void sendDrawMessageToServer(Shape shape) {
		out.println("DRAW " + shape.toString());
	}

//	public void sendMoveCommand(int id, int ox, int oy, int nx, int ny) {
//		out.println("MOVE " + id + " " + ox + " " + oy + " " + nx + " " + ny + " ");
//	}

	public void sendMoveCommand(int id, int dy, int dx) {
		out.println("MOVE " + id + " " + dy + " " + dx);
	}


	public void sendRecolorCommand(int id, int newColor) {
		out.println("RECOLOR " + id + " " + newColor);
	}

	public void sendDeleteCommand(int id) {
		out.println("DELETE " + id);
	}
	
}
