import java.awt.*;
import java.util.Arrays;

public class MessageParser {
    public static Shape parseShape(String[] shapeParams) {

        String shapeType = shapeParams[0];
        System.out.println("shapeType: " + shapeType);
        Shape shape = null;
        if (shapeType.equals("polyline")) {
            int x1 = Integer.parseInt(shapeParams[1]);
            int y1 = Integer.parseInt(shapeParams[2]);
            Color color = new Color(Integer.parseInt(shapeParams[shapeParams.length-1]));
            shape = new Polyline(x1, y1, color);

            for (int i = 3; i < shapeParams.length-1; i+=2){
                ((Polyline) shape).addNewPoint(
                        Integer.parseInt(shapeParams[i]),
                        Integer.parseInt(shapeParams[i+1])
                );
            }
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

        return shape;
    }
}
