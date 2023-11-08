import java.util.ArrayList;
import java.util.List;

public class Sketch {

        //need to have some kind of shape identification

        List<Shape> shapes;

        public Sketch() {
            shapes = new ArrayList<>();
        }

        public List<Shape> getShapes() {
            return shapes;
        }

        public void addShape(Shape shape) {
            shapes.add(shape);
        }

        public void removeShape(Shape shape) {
            shapes.remove(shape);
        }
}
