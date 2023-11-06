package core.utils.math;

public class VectorI {

    public int[] components;

    /**
     * Create a new vector with the given components.
     *
     * @param components The components of the vector.
     */
    public VectorI(int... components) {
        this.components = components;
    }

    /**
     * Copy a vector.
     *
     * @param v The vector to copy.
     */
    public VectorI(VectorI v) {
        this.components = v.components;
    }

    /**
     * Add two vectors together.
     *
     * @param other The vector to add to this one.
     * @return The sum of the two vectors.
     */
    public VectorI add(VectorI other) {
        if (other.components.length != components.length)
            throw new IllegalArgumentException("Vectors must have the same length.");
        int[] result = new int[components.length];
        for (int i = 0; i < components.length; i++) {
            result[i] = components[i] + other.components[i];
        }
        return new VectorI(result);
    }

    /**
     * Subtract two vectors.
     *
     * @param other The vector to subtract from this one.
     * @return The difference of the two vectors.
     */
    public VectorI subtract(VectorI other) {
        int[] result = new int[components.length];
        for (int i = 0; i < components.length; i++) {
            result[i] = components[i] - other.components[i];
        }
        return new VectorI(result);
    }

    /**
     * Multiply a vector by a scalar.
     *
     * @param scalar The scalar to multiply the vector by.
     * @return The product of the vector and the scalar.
     */
    public VectorI multiply(int scalar) {
        int[] result = new int[components.length];
        for (int i = 0; i < components.length; i++) {
            result[i] = components[i] * scalar;
        }
        return new VectorI(result);
    }

    /**
     * Divide a vector by a scalar.
     *
     * @param scalar The scalar to divide the vector by.
     * @return The quotient of the vector and the scalar.
     */
    public VectorI divide(int scalar) {
        int[] result = new int[components.length];
        for (int i = 0; i < components.length; i++) {
            result[i] = components[i] / scalar;
        }
        return new VectorI(result);
    }

    /**
     * Calculate the dot product of two vectors.
     *
     * @param other The vector to dot with this one.
     * @return The dot product of the two vectors.
     */
    public int dot(VectorI other) {
        int result = 0;
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
    public int length() {
        int result = 0;
        for (int i = 0; i < components.length; i++) {
            result += components[i] * components[i];
        }
        return (int) Math.sqrt(result);
    }

    /**
     * Calculate the squared length of a vector.
     *
     * <p>This is faster than calculating the length of a vector, and is useful for comparing the
     * relative lengths of two vectors.
     *
     * @return The squared length of the vector.
     */
    public int length2() {
        int result = 0;
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
    public VectorI normalize() {
        return divide(length());
    }

    /**
     * Get the component of the vector at the given index.
     *
     * @param index The index of the component to get.
     * @return The component at the given index.
     */
    public int get(int index) {
        return components[index];
    }

    /**
     * Set the component of the vector at the given index and return itself.
     *
     * @param index The index of the component to set.
     * @param value The value to set the component to.
     * @return The vector itself.
     */
    public VectorI set(int index, int value) {
        components[index] = value;
        return this;
    }

    /**
     * Copy the vector.
     *
     * @return A copy of the vector.
     */
    public VectorI copy() {
        return new VectorI(this.components);
    }
}
