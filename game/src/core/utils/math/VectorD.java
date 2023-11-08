package core.utils.math;

public class VectorD {

    public double[] components;

    /**
     * Create a new vector with the given components.
     *
     * @param components The components of the vector.
     */
    public VectorD(double... components) {
        this.components = components;
    }

    /**
     * Copy a vector.
     *
     * @param v The vector to copy.
     */
    public VectorD(VectorD v) {
        this.components = v.components;
    }

    /**
     * Add two vectors together.
     *
     * @param other The vector to add to this one.
     * @return The sum of the two vectors.
     */
    public VectorD add(VectorD other) {
        if (other.components.length != components.length)
            throw new IllegalArgumentException("Vectors must have the same length.");
        double[] result = new double[components.length];
        for (int i = 0; i < components.length; i++) {
            result[i] = components[i] + other.components[i];
        }
        return new VectorD(result);
    }

    /**
     * Subtract two vectors.
     *
     * @param other The vector to subtract from this one.
     * @return The difference of the two vectors.
     */
    public VectorD subtract(VectorD other) {
        double[] result = new double[components.length];
        for (int i = 0; i < components.length; i++) {
            result[i] = components[i] - other.components[i];
        }
        return new VectorD(result);
    }

    /**
     * Multiply a vector by a scalar.
     *
     * @param scalar The scalar to multiply the vector by.
     * @return The product of the vector and the scalar.
     */
    public VectorD multiply(double scalar) {
        double[] result = new double[components.length];
        for (int i = 0; i < components.length; i++) {
            result[i] = components[i] * scalar;
        }
        return new VectorD(result);
    }

    /**
     * Divide a vector by a scalar.
     *
     * @param scalar The scalar to divide the vector by.
     * @return The quotient of the vector and the scalar.
     */
    public VectorD divide(double scalar) {
        double[] result = new double[components.length];
        for (int i = 0; i < components.length; i++) {
            result[i] = components[i] / scalar;
        }
        return new VectorD(result);
    }

    /**
     * Calculate the dot product of two vectors.
     *
     * @param other The vector to dot with this one.
     * @return The dot product of the two vectors.
     */
    public double dot(VectorD other) {
        double result = 0;
        for (int i = 0; i < components.length; i++) {
            result += components[i] * other.components[i];
        }
        return result;
    }

    /**
     * Calculate the cross product of two vectors.
     *
     * @return The cross product of the two vectors.
     */
    public double length() {
        double result = 0;
        for (int i = 0; i < components.length; i++) {
            result += components[i] * components[i];
        }
        return (double) Math.sqrt(result);
    }

    /**
     * Calculate the squared length of a vector.
     *
     * <p>This is faster than calculating the length of a vector, and is useful for comparing the
     * relative lengths of two vectors.
     *
     * @return The squared length of the vector.
     */
    public double length2() {
        double result = 0;
        for (int i = 0; i < components.length; i++) {
            result += components[i] * components[i];
        }
        return result;
    }

    /**
     * Normalize a vector.
     *
     * @return The normalized vector.
     */
    public VectorD normalize() {
        return divide(length());
    }

    /**
     * Get the component of the vector at the given index.
     *
     * @param index The index of the component to get.
     * @return The component at the given index.
     */
    public double get(int index) {
        return components[index];
    }

    /**
     * Set the component of the vector at the given index and return itself.
     *
     * @param index The index of the component to set.
     * @param value The value to set the component to.
     * @return The vector itself.
     */
    public VectorD set(int index, double value) {
        components[index] = value;
        return this;
    }

    /**
     * Copy the vector.
     *
     * @return A copy of the vector.
     */
    public VectorD copy() {
        return new VectorD(this.components);
    }

    public double x() {
        return this.components[0];
    }

    public double y() {
        return this.components[1];
    }

    public double z() {
        return this.components[2];
    }

    public double w() {
        return this.components[3];
    }

    public double r() {
        return this.components[0];
    }

    public double g() {
        return this.components[1];
    }

    public double b() {
        return this.components[2];
    }

    public double a() {
        return this.components[3];
    }

    public void x(double x) {
        this.components[0] = x;
    }

    public void y(double y) {
        this.components[1] = y;
    }

    public void z(double z) {
        this.components[2] = z;
    }

    public void w(double w) {
        this.components[3] = w;
    }

    public void r(double r) {
        this.components[0] = r;
    }

    public void g(double g) {
        this.components[1] = g;
    }

    public void b(double b) {
        this.components[2] = b;
    }

    public void a(int a) {
        this.components[3] = a;
    }
}
