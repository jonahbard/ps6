import java.awt.*;
import java.net.*;
import java.util.*;
import java.io.*;

/**
 * A server to handle sketches: getting requests from the clients,
 * updating the overall state, and passing them on to the clients
 *
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; revised Winter 2014 to separate SketchServerCommunicator
 */
public class SketchServer {

	int nextID = 0;
	private ServerSocket listen;						// for accepting connections
	private ArrayList<SketchServerCommunicator> comms;	// all the connections with clients
	private Sketch sketch;								// the state of the world

	/**
	 * Create the sketch server, with an empty sketch on a serversocket
	 * @param listen
	 */
	public SketchServer(ServerSocket listen) {
		this.listen = listen;
		sketch = new Sketch();
		comms = new ArrayList<>();
	}

	/**
	 * Get the sketch, synchronously
	 * @return
	 */
	public synchronized Sketch getSketch() {
		return sketch;
	}

	/**
	 * Return the next ID to use, and increment the counter synchronously
	 * @return
	 */
	public synchronized int getNextIDAndIncrement() {
		int curID = nextID;
		nextID++;
		return curID;
	}

	/**
	 * The usual loop of accepting connections and firing off new threads to handle them
	 */
	public void getConnections() throws IOException {
		System.out.println("server ready for connections");
		while (true) {
			SketchServerCommunicator comm = new SketchServerCommunicator(listen.accept(), this);
			comm.setDaemon(true);
			comm.start();
			addCommunicator(comm);
		}
	}

	//BELOW: 4 functions that adjust the sketch when given Shape ID / other relevant parameters
	public synchronized void addShape(int id, Shape shape){
		sketch.addShape(id, shape); // need to
		System.out.println("added shape in sketch: " + shape);

	}

	public synchronized void removeShape(int id){
		sketch.removeShape(id);
		System.out.println("removed shape id: " + id);
	}

	public synchronized void moveShape(int id, int dx, int dy){
		sketch.moveShape(id, dx, dy);
		System.out.println("moved shape in sketch. id: " + id + " moved by " + dx + ", "+ dy);
	}

	public synchronized void recolorShape(int id, Color color){
		sketch.recolorShape(id, color);
		System.out.println("recolored shape in sketch. id: " + id + " color: "+ color);
	}


	/**
	 * Adds the communicator to the list of current communicators
	 */
	public synchronized void addCommunicator(SketchServerCommunicator comm) {
		comms.add(comm);
	}

	/**
	 * Removes the given communicator from the list of current communicators
	 */
	public synchronized void removeCommunicator(SketchServerCommunicator comm) {
		comms.remove(comm);
	}

	/**
	 * Sends a message from the one communicator to all editors
	 */
	public synchronized void broadcast(String msg) {
		for (SketchServerCommunicator comm : comms) {
			comm.send(msg);
		}
	}

	/***
	 * run server
	 */
	public static void main(String[] args) throws Exception {
		new SketchServer(new ServerSocket(4242)).getConnections();
	}
}