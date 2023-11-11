import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * A multi-segment Shape, with straight lines connecting "joint" points -- (x1,y1) to (x2,y2) to (x3,y3) ...
 * 
 * @author Chris Bailey-Kellogg, Dartmouth CS 10, Spring 2016
 * @author CBK, updated Fall 2016
 */
public class Polyline implements Shape {


	private List<Segment> segments; // a list of Segment shapes of which the polyline consists
	private Color color;

	/***
	 * constructor for a polyline: instantiating the list of segments, setting the first point
	 * @param x1
	 * @param y1
	 * @param color
	 */
	public Polyline(int x1, int y1, Color color) {
		segments = new ArrayList<>();
		segments.add(new Segment(x1, y1, color));
		this.color = color;
	}

	/***
	 * iterate through each segment and call its move method
	 * @param dx
	 * @param dy
	 */
	@Override
	public void moveBy(int dx, int dy) {
		for (Segment s: segments){
			s.moveBy(dx, dy);
		}
	}

	/***
	 * when the user is on DRAW mode and is dragging for a polyline,
	 * add each new point
	 * @param ox
	 * @param oy
	 * @param nx
	 * @param ny
	 */
	@Override
	public void drawDrag(int ox, int oy, int nx, int ny) {
		addNewPoint(nx, ny);
	}

	/***
	 * finish the in-progress segment at the end of the polyline,
	 * and append a new half-segment with the given points
	 * @param nx x-coordinate of point to be added to polyline
	 * @param ny y-coordinate of point to be added to polyline
	 */
	public void addNewPoint(int nx, int ny) {
		segments.get(segments.size()-1).setEnd(nx, ny);
		segments.add(new Segment(nx, ny, color));
	}


	@Override
	public Color getColor() {
		return color;
	}

	/***
	 * for each segment, set color to given color parameter
	 */
	@Override
	public void setColor(Color color) {
		this.color = color;
		for (Segment segment : segments) {
			segment.setColor(color);
		}
	}

	/***
	 * for each segment, check if segment contains point
	 */
	@Override
	public boolean contains(int x, int y) {
		for (Segment s: segments){
			if (s.contains(x, y)){
				return true;
			}
		}
		return false;
	}

	/**
	 * draw each segment individually
	 * @param g graphics
	 */
	@Override
	public void draw(Graphics g) {
		for (Segment s: segments) {
			s.draw(g);
		}
	}

	/**
	 * iterate through list and append each segment start to return string.
	 * last segment end point is not needed since the last segment will
	 * always just contain start and not end coordinates.
	 * finally, append the color.
	 * @return string representation of polyline used in message passing
	 */
	@Override
	public String toString() {
		String returnLine = "polyline ";

		// iterate through list and append each segment start to return string
		for (Segment s: segments) returnLine += s.getXStart() + " " + s.getYStart() + " ";

		returnLine += color.getRGB();

		return returnLine;
	}
}
