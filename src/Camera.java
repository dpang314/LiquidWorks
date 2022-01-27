public class Camera {
    private int scale = 100;
    private Matrix P;
    private int WIDTH;
    private int HEIGHT;

    public Camera(int WIDTH, int HEIGHT) {
        this.WIDTH = WIDTH;
        this.HEIGHT = HEIGHT;
    }

    // Obtained by multiplying rotation matrices in all directions
    public static Matrix getRotationMatrix(double degreesX,
                                           double degreesY,
                                           double degreesZ) {
        double radiansX = Math.toRadians(degreesX);
        double radiansY = Math.toRadians(degreesY);
        double radiansZ = Math.toRadians(degreesZ);
        Matrix rotationX = new Matrix(new double[][]{
                {Math.cos(radiansX), -Math.sin(radiansX), 0},
                {Math.sin(radiansX), Math.cos(radiansX), 0},
                {0, 0, 1}
        });

        Matrix rotationY = new Matrix(new double[][]{
                {Math.cos(radiansY), 0, Math.sin(radiansY)},
                {0, 1, 0},
                {-Math.sin(radiansY), 0, Math.cos(radiansY)}
        });

        Matrix rotationZ = new Matrix(new double[][]{
                {1, 0, 0},
                {0, Math.cos(radiansZ), -Math.sin(radiansZ)},
                {0, Math.sin(radiansZ), Math.cos(radiansZ)},
        });

        return rotationX.leftMultiply(rotationY).leftMultiply(rotationZ);
    }

    public void setZoom(int zoom) {
        this.scale = zoom;
    }

    public Triangle convert(Triangle triangle) {
        // These variables are independent, but I chose to
        // relate them to the scale.
        // These variables are the typical convention for
        // working with orthographic projections
        int RIGHT = scale;
        int LEFT = -scale;
        int TOP = scale;
        int BOTTOM = -scale;
        int NEAR = scale;
        int FAR = -scale;
        P = new Matrix(new double[][]{
                {2.0 / (RIGHT - LEFT), 0, 0, -1.0 *
                        (RIGHT + LEFT) / (RIGHT - LEFT)},
                {0, 2.0 / (TOP - BOTTOM), 0, -1.0 *
                        (TOP + BOTTOM) / (TOP - BOTTOM)},
                {0, 0, -2.0 / (FAR - NEAR), -1.0 *
                        (FAR + NEAR) / (FAR - NEAR)},
                {0, 0, 0, 1},
        });
        Triangle convertedTriangle = new Triangle(
                triangle.getVertex1().worldToPixel(P).scale(HEIGHT),
                triangle.getVertex2().worldToPixel(P).scale(HEIGHT),
                triangle.getVertex3().worldToPixel(P).scale(HEIGHT));
        return convertedTriangle;
    }
}
