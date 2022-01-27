public class Vector3D {
    private double x, y, z;

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return this.x;
    }

    public double getY() {
        return this.y;
    }

    public double getZ() {
        return this.z;
    }

    public Vector3D scale(double scalar) {
        return new Vector3D(x * scalar,
                y * scalar, z * scalar);
    }

    public Vector3D leftMultiply(Matrix m) {
        Vector3D newVector = m.getVector(0).scale(x)
                .add(m.getVector(1).scale(y)
                        .add(m.getVector(2).scale(z)));
        this.x = newVector.getX();
        this.y = newVector.getY();
        this.z = newVector.getZ();
        return this;
    }

    public Vector3D worldToPixel(Matrix m) {
        Vector3D vector = m.getVector(0).scale(x)
                .add(m.getVector(1).scale(y)
                        .add(m.getVector(2).scale(z))
                        .add(m.getVector(3)));
        return vector;
    }

    public Vector3D add(Vector3D v) {
        return new Vector3D(x + v.getX(),
                y + v.getY(), z + v.getZ());
    }

    public Vector3D increment(double s) {
        x += s;
        y += s;
        z += s;
        return this;
    }
}
