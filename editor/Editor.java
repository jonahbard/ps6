import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

/**
 * Client-server graphical editor
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012; loosely based on CS 5 code by Tom Cormen
 * @author CBK, winter 2014, overall structure substantially revised
 * @author Travis Peters, Dartmouth CS 10, Winter 2015; remove EditorCommunicatorStandalone (use echo server for testing)
 * @author CBK, spring 2016 and Fall 2016, restructured Shape and some of the GUI
 * @author Jonah Bard, Daniel Katz
 */

public class Editor extends JFrame {	
	private static String serverIP = "localhost";			// IP address of sketch server
	// "localhost" for your own machine;
	// or ask a friend for their IP address

	private static final int width = 800, height = 800;		// canvas size

	// Current settings on GUI
	public enum Mode {
		DRAW, MOVE, RECOLOR, DELETE
	}

	private Mode mode = Mode.DRAW;				// drawing/moving/recoloring/deleting objects
	private String shapeType = "ellipse";		// type of object to add and default it to ellipse

	private Color color = Color.black;			// current drawing color that defaults to black

	// Drawing state
	// these are remnants of my implementation; take them as possible suggestions or ignore them
	private Shape curr = null;					// current shape (if any) being drawn
	private Sketch sketch;						// holds and handles all the completed objects
	private int movingId = -1;					// current shape id (if any; else -1) being moved
	private Point drawFrom = null;				// where the drawing started
	private Point moveFrom = null;				// where object is as it's being dragged


	// Communication
	private EditorCommunicator comm;			// communication with the sketch server

	public Editor() {
		super("Graphical Editor");

		sketch = new Sketch();

		// Connect to server
		comm = new EditorCommunicator(serverIP, this);
		comm.start();

		// Helpers to create the canvas and GUI (buttons, etc.)
		JComponent canvas = setupCanvas();
		JComponent gui = setupGUI();

		// Put the buttons and canvas together into the window
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(canvas, BorderLayout.CENTER);
		cp.add(gui, BorderLayout.NORTH);

		// Usual initialization
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
	}

	/**
	 * Creates a component to draw into
	 */
	private JComponent setupCanvas() {
		JComponent canvas = new JComponent() {
			public void paintComponent(Graphics g) {
			super.paintComponent(g);
			drawSketch(g);
			}
		};
		
		canvas.setPreferredSize(new Dimension(width, height));

		canvas.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent event) {
				handlePress(event.getPoint());
			}

			public void mouseReleased(MouseEvent event) {
				handleRelease();
			}
		});		

		canvas.addMouseMotionListener(new MouseAdapter() {
			public void mouseDragged(MouseEvent event) {
				handleDrag(event.getPoint());
			}
		});
		
		return canvas;
	}

	/**
	 * Creates a panel with all the buttons
	 */
	private JComponent setupGUI() {
		// Select type of shape
		String[] shapes = {"ellipse", "freehand", "rectangle", "segment"};
		JComboBox<String> shapeB = new JComboBox<String>(shapes);
		shapeB.addActionListener(e -> shapeType = (String)((JComboBox<String>)e.getSource()).getSelectedItem());

		// Select drawing/recoloring color
		// Following Oracle example
		JButton chooseColorB = new JButton("choose color");
		JColorChooser colorChooser = new JColorChooser();
		JLabel colorL = new JLabel();
		colorL.setBackground(Color.black);
		colorL.setOpaque(true);
		colorL.setBorder(BorderFactory.createLineBorder(Color.black));
		colorL.setPreferredSize(new Dimension(25, 25));
		JDialog colorDialog = JColorChooser.createDialog(chooseColorB,
				"Pick a Color",
				true,  //modal
				colorChooser,
				e -> { color = colorChooser.getColor(); colorL.setBackground(color); },  // OK button
				null); // no CANCEL button handler
		chooseColorB.addActionListener(e -> colorDialog.setVisible(true));

		// Mode: draw, move, recolor, or delete
		JRadioButton drawB = new JRadioButton("draw");
		drawB.addActionListener(e -> mode = Mode.DRAW);
		drawB.setSelected(true);
		JRadioButton moveB = new JRadioButton("move");
		moveB.addActionListener(e -> mode = Mode.MOVE);
		JRadioButton recolorB = new JRadioButton("recolor");
		recolorB.addActionListener(e -> mode = Mode.RECOLOR);
		JRadioButton deleteB = new JRadioButton("delete");
		deleteB.addActionListener(e -> mode = Mode.DELETE);
		ButtonGroup modes = new ButtonGroup(); // make them act as radios -- only one selected
		modes.add(drawB);
		modes.add(moveB);
		modes.add(recolorB);
		modes.add(deleteB);
		JPanel modesP = new JPanel(new GridLayout(1, 0)); // group them on the GUI
		modesP.add(drawB);
		modesP.add(moveB);
		modesP.add(recolorB);
		modesP.add(deleteB);

		// Put all the stuff into a panel
		JComponent gui = new JPanel();
		gui.setLayout(new FlowLayout());
		gui.add(shapeB);
		gui.add(chooseColorB);
		gui.add(colorL);
		gui.add(modesP);
		return gui;
	}

	/**
	 * Getter for the sketch instance variable
	 */
	public synchronized Sketch getSketch() {
		return sketch;
	}

	/**
	 * Draws all the shapes in the sketch,
	 * along with the object currently being drawn in this editor (not yet part of the sketch)
	 */
	public void drawSketch(Graphics g) {
			// Draw each shape in the sketch from the earliest to the latest added
		for (Shape shape : sketch.getListOfShapesLowToHigh()) {
			shape.draw(g);
		}

		// Draw the currently drawing shape on top
		if (curr != null) {
			curr.draw(g);
		}
	}

	// Helpers for event handlers
	
	/**
	 * Helper method for press at point
	 * In drawing mode, start a new object;
	 * in moving mode, set ID + moveFrom to set up dragging if clicked in a shape;
	 * in recoloring mode, (request to) change clicked shape's color
	 * in deleting mode, (request to) delete clicked shape
	 */
	private void handlePress(Point p) {
		// Get the x and y coordinates of the point
		int curX = (int) p.getX(), curY = (int) p.getY();
		switch (mode) {
			case DRAW -> {
                switch (shapeType) {
					// Start drawing each type of shape
                    case "ellipse" -> curr = new Ellipse(curX, curY, color);
                    case "freehand" -> curr = new Polyline(curX, curY, color);
                    case "rectangle" -> curr = new Rectangle(curX, curY, color);
                    case "segment" -> curr = new Segment(curX, curY, color);
                }
				// Set the draw from point to the current point
				drawFrom = p;
			}
			case MOVE -> {
				// Get the topmost shape on the click and set up the moving variables if it is not null
				Integer id = sketch.getIDOfShapeOnTop(curX, curY);
				if (id != null) {
					movingId = id;
					moveFrom = p;
				}
			}
			case RECOLOR -> {
				// Get the topmost shape on the click and send recolor command if it exists
				Integer id = sketch.getIDOfShapeOnTop(curX, curY);
				if (id != null) {
					comm.sendRecolorMessageToServer(id, color.getRGB());
				}
			}
			case DELETE -> {
				// Get the topmost shape on the click and send delete command if it exists
				Integer id = sketch.getIDOfShapeOnTop(curX, curY);
				if (id != null) {
					comm.sendDeleteMessageToServer(id);
				}
			}
		}

		// Repaint the canvas after potential changes
		repaint();
	}


	/**
	 * Helper method for drag to new point
	 * In drawing mode, update the other corner of the object;
	 * in moving mode, (request to) drag the object
	 */
	private void handleDrag(Point p) {
		// Get the x and y coordinates of the point
		int curX = (int) p.getX(), curY = (int) p.getY();

		switch (mode) {
			case DRAW -> {
				// Draw the current shape from the drawFrom point to the current point
				if (curr != null && drawFrom != null) {
					int ox = (int) drawFrom.getX(), oy = (int) drawFrom.getY();
					curr.drawDrag(ox, oy, curX, curY);
				}
			}
			case MOVE -> {
				if (movingId != -1 && moveFrom != null) {
					// Send the movement command to the server and update the moveFrom point
					int ox = (int) moveFrom.getX(), oy = (int) moveFrom.getY();
					comm.sendMoveMessageToServer(movingId, curX - ox, curY - oy);
					moveFrom = p;
				}
			}
		}

		// Repaint the canvas after potential changes
		repaint();
	}

	/**
	 * Helper method for release
	 * In drawing mode, pass the add new object (request) on to the server;
	 * in moving mode, release it		
	 */
	private void handleRelease() {
		switch (mode) {
			case DRAW -> {
				// Send the shape to the server and clear the current drawFrom
				comm.sendDrawMessageToServer(curr);
				drawFrom = null;
			}
			case MOVE -> {
				// Moving is over so reset the moving variables
				movingId = -1;
				moveFrom = null;
			}
		}

		// Repaint the canvas after potential changes
		repaint();
	}

	/**
	 * Move a shape in the sketch by the given difference
	 */
	public synchronized void moveShape(int id, int dx, int dy) {
		sketch.getShape(id).moveBy(dx, dy);
	}

	/**
	 * Add a shape to the sketch with the given id
	 */
	public synchronized void addShape(int id, Shape shape) {
		sketch.addShape(id, shape);
	}

	/**
	 * Recolor a shape in the sketch
	 */
	public synchronized void recolorShape(int id, Color newColor) {
		sketch.getShape(id).setColor(newColor);
	}

	/**
	 * Delete a shape from the sketch
	 */
	public synchronized void deleteShape(int id) {
		sketch.removeShape(id);
	}

	/**
	 * Repaint the canvas
	 */
	public synchronized void callRepaint() {
		repaint();
	}

	/**
	 * Clear the current shape
     * Used when the communicator finally gets the shape back from the server
	 */
	public synchronized void clearCurrentShape() {
		curr = null;
	}

	/**
	 * Run the editor
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				new Editor();
			}
		});	
	}
}
