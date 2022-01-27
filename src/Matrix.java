public class Matrix {
    private Vector3D[] vectors;

    // Matrix is made of column vectors
    public Matrix(Vector3D[] vectors) {
        this.vectors = vectors;
    }

    public Matrix(double[][] values) {
        vectors = new Vector3D[values[0].length];
        for (int i = 0; i < values[0].length; i++) {
            vectors[i] = new Vector3D(values[0][i],
                    values[1][i], values[2][i]);
        }
    }

    public Vector3D getVector(int index) {
        if (index >= vectors.length) {
            return null;
        }
        return vectors[index];
    }

    public Matrix leftMultiply(Matrix m) {
        Vector3D[] vectors = new
                Vector3D[this.vectors.length];
        for (int i = 0; i < this.vectors.length; i++) {
            vectors[i] = this.getVector(i).leftMultiply(m);
        }
        return new Matrix(vectors);
    }
}
