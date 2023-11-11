import java.awt.*;
import java.util.List;
import java.util.TreeMap;

/**
 * Class for storing the shapes in the sketch.
 * This class is synchronized where possible, so it can be used by multiple threads.
 * @author Jonah Bard, Daniel Katz
 */

public class Sketch {

    //need to have some kind of shape identification

    private TreeMap<Integer, Shape> shapes;

    /**
     * Create a new sketch with no shapes
     */
    public Sketch() {
        shapes = new TreeMap<>();
    }

    /**
     * Get the shape with the given ID
     * @param id
     * @return
     */
    public synchronized Shape getShape(int id) {
            return shapes.get(id);
    }

    /**
     * Get shapes low to high based on their IDs (and thus drawing order)
     * @return
     */
    public synchronized List<Shape> getListOfShapesLowToHigh() {
        return shapes.navigableKeySet()
                .stream()
                .map(key -> shapes.get(key))
                .toList();
    }

    /**
     * Add a shape to the sketch with the given ID
     * @param id
     * @param shape
     */
    public synchronized void addShape(Integer id, Shape shape) {
            shapes.put(id, shape);
    }

    /**
     * Move the shape with the given ID by the given amount
     * @param id
     * @param dx
     * @param dy
     */
    public synchronized void moveShape(int id, int dx, int dy){
        shapes.get(id).moveBy(dx, dy);
    }

    /**
     *
     * @param id
     * @param color
     */
    public synchronized void recolorShape(int id, Color color){
        shapes.get(id).setColor(color);
    }

    public synchronized void removeShape(int id) {
        shapes.remove(id);
    }

    /***
     * get the ID of the shape corresponding to the point pressed.
     * for our interface, by default, this is the topmost (in the theoretical shape stack) shape
     * that contains the point.
     */
    public Integer getIDOfShapeOnTop(int x, int y) {

        //iterate through each shape top-to-bottom, since high IDs are on top
        for (Integer id : shapes.descendingKeySet()) {
            Shape shape = shapes.get(id);
            if (shape.contains(x, y)) return id;
        }
        return null;
    }

    /***
     * converts the entire sketch to a string. all shapes start with a vertical pipe
     * Pipe operators are for seperating individual shape commands so we need to get rid of the preceding one
     * @return
     */
    public String toString() {
        String out = "";
        for (int id : shapes.navigableKeySet()) {
            Shape shape = shapes.get(id);
            out += "|" + id + " " + shape.toString();
        }
        return !shapes.isEmpty() ? out.substring(1) : "";
    }
}
