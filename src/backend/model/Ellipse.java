package backend.model;

import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

/**
 * modelo 2D de una elipse.
 */
public class Ellipse extends Figure {

    protected final Point centerPoint;
    protected final double mayorAxis;
    protected final double minorAxis;

    /**
     * Nueva instancia de una elipse.
     * @param centerPoint punto 2D del centro de la elipse.
     * @param mayorAxis longitud del eje mayor.
     * @param minorAxis longitud del eje menor.
     */
    public Ellipse(Point centerPoint, double mayorAxis, double minorAxis) {
        if(centerPoint == null || mayorAxis == 0 || minorAxis == 0){
            throw new IllegalArgumentException("Formación de figura inválida");
        }
        this.centerPoint = centerPoint;
        this.mayorAxis = mayorAxis;
        this.minorAxis = minorAxis;
    }

    public Point getCenterPoint() {
        return centerPoint;
    }

    public double getMayorAxis() {
        return mayorAxis;
    }

    public double getMinorAxis() {
        return minorAxis;
    }

    @Override
    public boolean contains(Point p) {
        double deltaX = p.getX() - centerPoint.getX();
        double deltaY = p.getY() - centerPoint.getY();
        double a = mayorAxis / 2;
        double b = minorAxis / 2;
        return ((Math.pow(deltaX,2) / Math.pow(a,2)) + (Math.pow(deltaY,2) / Math.pow(b,2))) < 1;
    }

    @Override
    public boolean isInside(Rectangle container) {
        return container.contains(getTopLeft()) && container.contains(getBottomRight());

    }

    /**
     * Realiza el cálculo para el punto TopLeft del rectángulo que formo la elipse.
     * @return devuelve una instancia nueva de Point correspondiente al TopLeft
     */
    private Point getTopLeft() {
        return new Point(centerPoint.getX() - mayorAxis / 2,
                centerPoint.getY() - minorAxis / 2);
    }
    /**
     * Realiza el cálculo para el punto BottomRight del rectángulo que formó la elipse.
     * @return devuelve una instancia nueva de Point correspondiente al BottomRight.
     */
    private Point getBottomRight() {
        return new Point(centerPoint.getX() + mayorAxis / 2,
                centerPoint.getY() + minorAxis / 2);
    }

    @Override
    protected List<Point> getPoints() {
        List<Point> toReturn = new ArrayList<>();
        toReturn.add(centerPoint);
        return toReturn;
    }

    @Override
    public void draw(GraphicsContext gc) {
        Point topLeft = getTopLeft();
        gc.strokeOval(topLeft.getX(),
                topLeft.getY(),
                mayorAxis, minorAxis);
        gc.fillOval(topLeft.getX(),
                topLeft.getY(),
                mayorAxis, minorAxis);
    }

    @Override
    public String toString() {
        return String.format("Elipse [Centro: %s, Eje mayor: %.2f, Eje menor: %.2f]", centerPoint, mayorAxis, minorAxis);
    }
}
