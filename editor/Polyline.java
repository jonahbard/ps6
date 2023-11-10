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


	private List<Segment> segments;
	private Color color;

	public Polyline(int x1, int y1, Color color) {
		segments = new ArrayList<>();
		segments.add(new Segment(x1, y1, color));
		this.color = color;
	}


	@Override
	public void moveBy(int dx, int dy) {
	}

	@Override
	public void drawDrag(int ox, int oy, int nx, int ny) {
		segments.get(segments.size()-1).setEnd(nx, ny);
		segments.add(new Segment(nx, ny, color));

	}


	@Override
	public Color getColor() {
		return color;
	}

	@Override
	public void setColor(Color color) {
		this.color = color;
	}
	
	@Override
	public boolean contains(int x, int y) {
		for (Segment s: segments){
			if (s.contains(x, y)){
				return true;
			}
		}
		return false;
	}

	@Override
	public void draw(Graphics g) {
		for (Segment s: segments) {
			s.draw(g);
		}
	}

	@Override
	public String toString() {
		String returnLine = "polyline ";

		for (Segment s: segments) {
			returnLine += "\n\t" + s.toString();
		}
		returnLine += "\n polyline color:" + color.getRGB();

		return returnLine;
	}
}
