package core.utils.math;

public class VectorF {

    public float[] components;

    /**
     * Create a new vector with the given components.
     *
     * @param components The components of the vector.
     */
    public VectorF(float... components) {
        this.components = components;
    }

    /**
     * Copy a vector.
     *
     * @param v The vector to copy.
     */
    public VectorF(VectorF v) {
        this.components = v.components;
    }

    /**
     * Add two vectors together.
     *
     * @param other The vector to add to this one.
     * @return The sum of the two vectors.
     */
    public VectorF add(VectorF other) {
        if (other.components.length != components.length)
            throw new IllegalArgumentException("Vectors must have the same length.");
        float[] result = new float[components.length];
        for (int i = 0; i < components.length; i++) {
            result[i] = components[i] + other.components[i];
        }
        return new VectorF(result);
    }

    /**
     * Subtract two vectors.
     *
     * @param other The vector to subtract from this one.
     * @return The difference of the two vectors.
     */
    public VectorF subtract(VectorF other) {
        float[] result = new float[components.length];
        for (int i = 0; i < components.length; i++) {
            result[i] = components[i] - other.components[i];
        }
        return new VectorF(result);
    }

    /**
     * Multiply a vector by a scalar.
     *
     * @param scalar The scalar to multiply the vector by.
     * @return The product of the vector and the scalar.
     */
    public VectorF multiply(float scalar) {
        float[] result = new float[components.length];
        for (int i = 0; i < components.length; i++) {
            result[i] = components[i] * scalar;
        }
        return new VectorF(result);
    }

    /**
     * Divide a vector by a scalar.
     *
     * @param scalar The scalar to divide the vector by.
     * @return The quotient of the vector and the scalar.
     */
    public VectorF divide(float scalar) {
        float[] result = new float[components.length];
        for (int i = 0; i < components.length; i++) {
            result[i] = components[i] / scalar;
        }
        return new VectorF(result);
    }

    /**
     * Calculate the dot product of two vectors.
     *
     * @param other The vector to dot with this one.
     * @return The dot product of the two vectors.
     */
    public float dot(VectorF other) {
        float result = 0;
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
    public float length() {
        float result = 0;
        for (int i = 0; i < components.length; i++) {
            result += components[i] * components[i];
        }
        return (float) Math.sqrt(result);
    }

    /**
     * Calculate the squared length of a vector.
     *
     * <p>This is faster than calculating the length of a vector, and is useful for comparing the
     * relative lengths of two vectors.
     *
     * @return The squared length of the vector.
     */
    public float length2() {
        float result = 0;
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
    public VectorF normalize() {
        return divide(length());
    }

    /**
     * Get the component of the vector at the given index.
     *
     * @param index The index of the component to get.
     * @return The component at the given index.
     */
    public float get(int index) {
        return components[index];
    }

    /**
     * Set the component of the vector at the given index and return itself.
     *
     * @param index The index of the component to set.
     * @param value The value to set the component to.
     * @return The vector itself.
     */
    public VectorF set(int index, float value) {
        components[index] = value;
        return this;
    }

    /**
     * Copy the vector.
     *
     * @return A copy of the vector.
     */
    public VectorF copy() {
        return new VectorF(this.components);
    }

    public float x() {
        return this.components[0];
    }

    public float y() {
        return this.components[1];
    }

    public float z() {
        return this.components[2];
    }

    public float w() {
        return this.components[3];
    }

    public float r() {
        return this.components[0];
    }

    public float g() {
        return this.components[1];
    }

    public float b() {
        return this.components[2];
    }

    public float a() {
        return this.components[3];
    }

    public void x(float x) {
        this.components[0] = x;
    }

    public void y(float y) {
        this.components[1] = y;
    }

    public void z(float z) {
        this.components[2] = z;
    }

    public void w(float w) {
        this.components[3] = w;
    }

    public void r(float r) {
        this.components[0] = r;
    }

    public void g(float g) {
        this.components[1] = g;
    }

    public void b(float b) {
        this.components[2] = b;
    }

    public void a(float a) {
        this.components[3] = a;
    }
}
