import java.awt.*;
import java.awt.image.BufferedImage;

public class Triangle {
    private final Vector3D vertex1;
    private final Vector3D vertex2;
    private final Vector3D vertex3;

    public Triangle(Vector3D vertex1,
                    Vector3D vertex2,
                    Vector3D vertex3) {
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
        this.vertex3 = vertex3;
    }

    public void leftMultiply(Matrix m) {
        vertex1.leftMultiply(m);
        vertex2.leftMultiply(m);
        vertex3.leftMultiply(m);
    }

    public void increment(double s) {
        vertex1.increment(s);
        vertex2.increment(s);
        vertex3.increment(s);
    }

    public Vector3D getVertex1() {
        return vertex1;
    }

    public Vector3D getVertex2() {
        return vertex2;
    }

    public Vector3D getVertex3() {
        return vertex3;
    }

    private double edgeFunction(Vector3D vertex1,
                                Vector3D vertex2,
                                Vector3D point) {
        return (
                (point.getX() - vertex1.getX()) *
                (vertex2.getY() - vertex1.getY()) -
                (point.getY() - vertex1.getY()) *
                (vertex2.getX() - vertex1.getX())
        );
    }

    public boolean onEdge(Vector3D point) {
        return ((int) Math.abs(edgeFunction(vertex1, vertex2, point)) < 10 &&
                edgeFunction(vertex2, vertex3, point) >= 0 &&
                edgeFunction(vertex3, vertex1, point) >= 0) ||
                (edgeFunction(vertex1, vertex2, point) > 0 &&
                (int) Math.abs(edgeFunction(vertex2, vertex3, point)) < 10 &&
                edgeFunction(vertex3, vertex1, point) >= 0) ||
                (edgeFunction(vertex1, vertex2, point) >= 0 &&
                edgeFunction(vertex2, vertex3, point) >= 0 &&
                (int) Math.abs(edgeFunction(vertex3, vertex1, point)) < 10);
    }

    public boolean insideTriangle(Vector3D point) {
        return edgeFunction(vertex1, vertex2, point) > 0 &&
                edgeFunction(vertex2, vertex3, point) > 0 &&
                edgeFunction(vertex3, vertex1, point) > 0;
    }

    public BufferedImage project(double[][] zBuffer,
                                 BufferedImage screen, Color color) {
        // Finds bounding box to avoid looping over entire screen
        int xMin = (int) Math.floor(
                Math.min(vertex1.getX(),
                        Math.min(vertex2.getX(), vertex3.getX())));
        int yMin = (int) Math.floor(
                Math.min(vertex1.getY(),
                        Math.min(vertex2.getY(), vertex3.getY())));
        int xMax = (int) Math.ceil(
                Math.max(vertex1.getX(),
                        Math.max(vertex2.getX(), vertex3.getX())));
        int yMax = (int) Math.ceil(
                Math.max(vertex1.getY(),
                        Math.max(vertex2.getY(), vertex3.getY())));
        for (int i = Math.max(0, xMin);
             i < Math.min(screen.getWidth(), xMax); i++) {
            for (int j = Math.max(0, yMin);
                 j < Math.min(screen.getHeight(), yMax); j++) {
                Vector3D plane = calculatePlane();
                double k = -1 * (plane.getX() * this.getVertex1().getX() +
                        plane.getY() * this.getVertex1().getY() +
                        plane.getZ() * this.getVertex1().getZ());
                double z = -1 * (plane.getX() * i + plane.getY() * j + k);
                boolean valid = false;
                Color currentPixel = color;
                if (onEdge(new Vector3D(i, j, 0))) {
                    currentPixel = Color.BLACK;
                    valid = true;
                } else if (insideTriangle(new Vector3D(i, j, 0))) {
                    valid = true;
                }
                if (valid && z < zBuffer[i][j]) {
                    screen.setRGB(i, j, currentPixel.getRGB());
                }
            }
        }
        return screen;
    }

    public Vector3D calculatePlane() {
        Vector3D A = vertex2.add(vertex1.scale(-1));
        Vector3D B = vertex3.add(vertex1.scale(-1));
        double x = A.getY() * B.getZ() - A.getZ() * B.getY();
        double y = A.getZ() * B.getX() - A.getX() * B.getZ();
        double z = A.getX() * B.getY() - A.getY() * B.getX();
        return new Vector3D(x, y, z);
    }
}
