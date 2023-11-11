import java.awt.*;

/**
 * Class to deduplicate parsing code
 * @author Jonah Bard, Daniel Katz
 */

public class MessageParser {


    // static method that will be called from this class itself (not instance) to parse a shape
    // given String[] representing the shape part of the message sent to/from server.

    public static Shape parseShape(String[] shapeParams) {

        Shape shape = null; //declare shape

        // message format: SHAPE_TYPE X1 Y1 X2 Y2 COLOR
        // ^for polyline, there will be x3 y3, x4 y4, etc.

        String shapeType = shapeParams[0];

        int x1 = Integer.parseInt(shapeParams[1]);
        int y1 = Integer.parseInt(shapeParams[2]);

        //we need to call shapeParams.length-1 to get color, since
        // polylines have non-finite amount of elements in their array
        Color color = new Color(Integer.parseInt(shapeParams[shapeParams.length-1]));

        //for polyline, need to set the two start points (x1, y1) and then iterate through
        // the rest of the shapeParams array to add all points to the polyline
        if (shapeType.equals("polyline")) {

            shape = new Polyline(x1, y1, color); //instantiate shape based on x1, y1 that we read

            // add the rest of the points to the polyline
            for (int i = 3; i < shapeParams.length-1; i+=2){
                ((Polyline) shape).addNewPoint(
                        Integer.parseInt(shapeParams[i]),
                        Integer.parseInt(shapeParams[i+1])
                );
            }
        }

        // otherwise we can just parse the array directly into variables for x2 and y2,
        // and instantiate shape based on topLeft / bottomRight coordinates
        else {
            int x2 = Integer.parseInt(shapeParams[3]);
            int y2 = Integer.parseInt(shapeParams[4]);

            if (shapeType.equals("ellipse")) shape = new Ellipse(x1, y1, x2, y2, color);
            else if (shapeType.equals("rectangle")) shape = new Rectangle(x1, y1, x2, y2, color);
            else if (shapeType.equals("segment")) shape = new Segment(x1, y1, x2, y2, color);
        }

        //return the shape it has now instantiated
        return shape;
    }
}
