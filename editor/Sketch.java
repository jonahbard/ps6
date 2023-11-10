import java.awt.*;
import java.util.List;
import java.util.TreeMap;

public class Sketch {

        //need to have some kind of shape identification

        private TreeMap<Integer, Shape> shapes;

        public Sketch() {
            shapes = new TreeMap<>();
        }

//        public TreeMap<Integer, Shape> getShapes() {
//            return shapes;
//        }

        public synchronized Shape getShape(int id) {
                return shapes.get(id);
        }

        public synchronized List<Shape> getListOfShapesLowToHigh() {
            return shapes.navigableKeySet()
                    .stream()
                    .map(key -> shapes.get(key))
                    .toList();
        }

        public synchronized void addShape(Integer id, Shape shape) {
            shapes.put(id, shape);
        }


        public synchronized void moveShape(int id, int dx, int dy){
            shapes.get(id).moveBy(dx, dy);
        }

        public synchronized void recolorShape(int id, Color color){
            shapes.get(id).setColor(color);
        }

        public synchronized void removeShape(int id) {
            shapes.remove(id);
        }

        public Integer getIDOfShapeOnTop(int x, int y) {
            for (Integer id : shapes.descendingKeySet()) {
                Shape shape = shapes.get(id);
                if (shape.contains(x, y)) return id;
            }
            return null;
        }
}
