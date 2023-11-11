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
			// while not sending data TO the server, handle messages FROM the server
			//should constantly set the "sketch" in Editor to be the SketchServer's sketch
			// Handle messages
			String line;
			while ((line = in.readLine()) != null) {
				handleMessageFromServer(line);
			}
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			System.out.println("server hung up");
		}
	}

	/**
     * Handles a message from the server, by parsing it and then doing the appropriate action
     * @param message
     */
	public void handleMessageFromServer(String message) {

		// Split the message into commands
		String[] commands = message.split(" ");

		// If the message is a SKETCH message, then parse the individual shapes add them all to the sketch.
		// If the command only has SKETCH, it means the canvas is currently empty
		if (commands[0].equals("SKETCH") && commands.length > 1) {
			// Re-split the message because the individual shapes are separated by "|", not " "
			commands = message.split(" ", 2);

			// Get the individual shape commands
			String[] shapeCommandStrings = commands[1].split("\\|");

			// Add each shape to the canvas
			for (String shapeCommandString : shapeCommandStrings) {
				String[] shapeCommands = shapeCommandString.split(" ");
				int id = Integer.parseInt(shapeCommands[0]);
				Shape shape = MessageParser.parseShape(Arrays.copyOfRange(shapeCommands, 1, shapeCommands.length));
				editor.addShape(id, shape);
			}
		}

		// If the message is a DRAW message, then parse the shape and add it to the sketch
		// format: DRAW ID SHAPE_TYPE X1 Y1 X2 Y2 COLOR
		else if (commands[0].equals("DRAW")) {

			// Check if the shape has an ID or not (it only wouldn't have an ID on this end if we are using the echo server)
			boolean hasID = true;
			try {
				Integer.parseInt(commands[1]);
			}
			catch (Exception e) {
				hasID = false;
			}

			// Set the id or randomly generate one
			int id = hasID ? Integer.parseInt(commands[1]) : (int) (Math.random() * 1000);

			// Slice array depending on whether or not the shape was returned with an ID or not
			String[] shapeParams = Arrays.copyOfRange(commands, hasID ? 2 : 1, commands.length);

			Shape shape = MessageParser.parseShape(shapeParams);
			if (shape != null) editor.addShape(id, shape);
			editor.clearCurrentShape();
		}
		// If the message is a MOVE message, then call the editor to move the shape in sketch
		// format: MOVE ID OX OY NX NY
		else if (commands[0].equals("MOVE")){
			int id = Integer.parseInt(commands[1]);
			int dx = Integer.parseInt(commands[2]);
			int dy = Integer.parseInt(commands[3]);

			editor.moveShape(id, dx, dy);
		}

		// If the message is a RECOLOR message, then call the editor to recolor the shape in sketch
		// format: RECOLOR ID NEWCOLOR
		else if (commands[0].equals("RECOLOR")){
			int id = Integer.parseInt(commands[1]);
			Color color = new Color(Integer.parseInt(commands[2]));

			editor.recolorShape(id, color);

		}

		// If the message is a DELETE message, then call the editor to delete the shape in sketch
		// format: DELETE ID
		else if (commands[0].equals("DELETE")) {
			int id = Integer.parseInt(commands[1]);

			editor.deleteShape(id);
		}

		// Repaint the editor after state changing message has been processed
		editor.callRepaint();
	}


	// Send editor requests to the server

	public void sendDrawMessageToServer(Shape shape) {
		out.println("DRAW " + shape.toString());
	}

	public void sendMoveMessageToServer(int id, int dy, int dx) {
		out.println("MOVE " + id + " " + dy + " " + dx);
	}

	public void sendRecolorMessageToServer(int id, int newColor) {
		out.println("RECOLOR " + id + " " + newColor);
	}

	public void sendDeleteMessageToServer(int id) {
		out.println("DELETE " + id);
	}
	
}
