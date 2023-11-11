import java.awt.Color;
import java.awt.Graphics;

/**
 * A rectangle-shaped Shape
 * Defined by an upper-left corner (x1,y1) and a lower-right corner (x2,y2)
 * with x1<=x2 and y1<=y2
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Fall 2012
 * @author CBK, updated Fall 2016
 */
public class Rectangle implements Shape {
	private int x1, y1; // top left
	private int x2, y2; // bottom right
	private Color color;


	/**
	 * Create a rectangle with all corners
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param color
	 */
	public Rectangle(int x1, int y1, int x2, int y2, Color color){
		this.x1 = Math.min(x1, x2);
		this.y1 = Math.min(y1, y2);
		this.x2 = Math.max(x1, x2);
		this.y2 = Math.max(y1, y2);
		this.color = color;
	}

	/**
	 * Create a rectangle with no area
	 * @param x1
	 * @param y1
	 * @param color
	 */
	public Rectangle(int x1, int y1, Color color){
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x1;
		this.y2 = y1;
		this.color = color;
	}

	/**
	 * Move the rectangle by dx and dy
	 * @param dx
	 * @param dy
	 */
	@Override
	public void moveBy(int dx, int dy) {
		x1 += dx; y1 += dy;
		x2 += dx; y2 += dy;
	}

	/**
	 * Handle size while draw dragging
	 * @param ox
	 * @param oy
	 * @param nx
	 * @param ny
	 */
	@Override
	public void drawDrag(int ox, int oy, int nx, int ny) {
		setCorners(ox, oy, nx, ny);
	}

	/**
	 * Set the corners of the rectangle to the given coordinates for maximum size
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	public void setCorners(int x1, int y1, int x2, int y2) {
		// Ensure correct upper left and lower right
		this.x1 = Math.min(x1, x2);
		this.y1 = Math.min(y1, y2);
		this.x2 = Math.max(x1, x2);
		this.y2 = Math.max(y1, y2);
	}

	/**
	 * Get the color
	 * @return
	 */
	@Override
	public Color getColor() {
		return color;
	}

	/**
	 * Set the color
	 * @param color The shape's color
	 */
	@Override
	public void setColor(Color color) {
		this.color = color;
	}

	/**
	 * Check if the point is within the rectangle
	 * @param x
	 * @param y
	 * @return
	 */
	@Override
	public boolean contains(int x, int y) {
		return (x >= x1 && x <= x2 && y >= y1 && y <= y2);
	}

	/**
	 * Draw the rectangle
	 * @param g
	 */
	@Override
	public void draw(Graphics g) {
		g.setColor(color);
		g.fillRect(x1, y1, x2-x1, y2-y1);
	}

	/**
	 * Convert the rectangle into a string form for message passing
	 * @return
	 */
	public String toString() {
		return "rectangle "+x1+" "+y1+" "+x2+" "+y2+" "+color.getRGB();
	}
}



