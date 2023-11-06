package contrib.utils.math;

public class Vector {

    public float[] components;

    /**
     * Create a new vector with the given components.
     *
     * @param components The components of the vector.
     */
    public Vector(float... components) {
        this.components = components;
    }

    /**
     * Add two vectors together.
     *
     * @param other The vector to add to this one.
     * @return The sum of the two vectors.
     */
    public Vector add(Vector other) {
        if (other.components.length != components.length)
            throw new IllegalArgumentException("Vectors must have the same length.");
        float[] result = new float[components.length];
        for (int i = 0; i < components.length; i++) {
            result[i] = components[i] + other.components[i];
        }
        return new Vector(result);
    }

    /**
     * Subtract two vectors.
     *
     * @param other The vector to subtract from this one.
     * @return The difference of the two vectors.
     */
    public Vector subtract(Vector other) {
        float[] result = new float[components.length];
        for (int i = 0; i < components.length; i++) {
            result[i] = components[i] - other.components[i];
        }
        return new Vector(result);
    }

    /**
     * Multiply a vector by a scalar.
     *
     * @param scalar The scalar to multiply the vector by.
     * @return The product of the vector and the scalar.
     */
    public Vector multiply(float scalar) {
        float[] result = new float[components.length];
        for (int i = 0; i < components.length; i++) {
            result[i] = components[i] * scalar;
        }
        return new Vector(result);
    }

    /**
     * Divide a vector by a scalar.
     *
     * @param scalar The scalar to divide the vector by.
     * @return The quotient of the vector and the scalar.
     */
    public Vector divide(float scalar) {
        float[] result = new float[components.length];
        for (int i = 0; i < components.length; i++) {
            result[i] = components[i] / scalar;
        }
        return new Vector(result);
    }

    /**
     * Calculate the dot product of two vectors.
     *
     * @param other The vector to dot with this one.
     * @return The dot product of the two vectors.
     */
    public float dot(Vector other) {
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
    public Vector normalize() {
        return divide(length());
    }
}
