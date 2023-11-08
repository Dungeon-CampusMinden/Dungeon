package core.utils.math;

public class Matrix4f {

    private float[][] data;

    public Matrix4f() {
        this.data = new float[4][4];
    }

    public Matrix4f(float[][] data) {
        this.data = data;
    }

    public Matrix4f(Matrix4f matrix) {
        this.data = matrix.data;
    }

    public Matrix4f copy() {
        return new Matrix4f(this);
    }

    public float[][] data() {
        return this.data;
    }

    public float[] toOpenGL() {
        float[] result = new float[16];
        int index = 0;
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 4; row++) {
                result[index++] = this.data[row][col];
            }
        }
        return result;
    }

    public float get(int row, int col) {
        return this.data[row][col];
    }

    public void set(int row, int col, float value) {
        this.data[row][col] = value;
    }

    public Matrix4f add(Matrix4f matrix) {
        float[][] result = new float[4][4];
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 4; row++) {
                result[row][col] = this.data[row][col] + matrix.data[row][col];
            }
        }
        return new Matrix4f(result);
    }

    public Matrix4f subtract(Matrix4f matrix) {
        float[][] result = new float[4][4];
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 4; row++) {
                result[row][col] = this.data[row][col] - matrix.data[row][col];
            }
        }
        return new Matrix4f(result);
    }

    public Matrix4f multiply(Matrix4f second) {
        float[][] result = new float[4][4];
        for (int cs = 0; cs < 4; cs++) {
            for (int fr = 0; fr < 4; fr++) {
                float sum = 0.0f;
                for (int i = 0; i < 4; i++) {
                    sum += this.data[fr][i] * second.data[i][cs];
                }
                result[fr][cs] = sum;
            }
        }
        return new Matrix4f(result);
    }

    public Matrix4f multiply(float scalar) {
        float[][] result = new float[4][4];
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 4; row++) {
                result[row][col] = this.data[row][col] * scalar;
            }
        }
        return new Matrix4f(result);
    }

    public Vector4f multiply(Vector4f vector) {
        float[] result = new float[4];
        for (int row = 0; row < 4; row++) {
            float sum = 0.0f;
            for (int i = 0; i < 4; i++) {
                sum += this.data[row][i] * vector.get(i);
            }
            result[row] = sum;
        }
        return new Vector4f(result);
    }

    public Matrix4f divide(float scalar) {
        float[][] result = new float[4][4];
        float invScalar = 1.0f / scalar;
        for (int col = 0; col < 4; col++) {
            for (int row = 0; row < 4; row++) {
                result[row][col] = this.data[row][col] * invScalar;
            }
        }
        return new Matrix4f(result);
    }

    public Matrix4f transpose() {
        float[][] result = new float[4][4];
        for (int col = 0; col < 4; col++) {
            System.arraycopy(this.data[col], 0, result[col], 0, 4);
        }
        return new Matrix4f(result);
    }

    /**
     * Creates an orthographic projection matrix
     *
     * @param left
     * @param right
     * @param bottom
     * @param top
     * @param near
     * @param far
     * @return Matrix4f
     */
    public static Matrix4f orthographicProjection(
            float left, float right, float bottom, float top, float near, float far) {
        float[][] result = new float[4][4];
        result[0][0] = 2.0f / (right - left);
        result[1][1] = 2.0f / (top - bottom);
        result[2][2] = -2.0f / (far - near);
        result[0][3] = -(right + left) / (right - left);
        result[1][3] = -(top + bottom) / (top - bottom);
        result[2][3] = -(far + near) / (far - near);
        result[3][3] = 1.0f;
        return new Matrix4f(result);
    }

    /**
     * Creates a perspective projection matrix
     *
     * <p>TODO: Check if this is correct
     *
     * @param fov Field of view in degrees
     * @param aspectRatio Aspect ratio of the screen
     * @param near Near plane
     * @param far Far plane
     * @return Matrix4f
     */
    public static Matrix4f perspectiveProjection(
            float fov, float aspectRatio, float near, float far) {
        float[][] result = new float[4][4];
        float radians = (float) Math.toRadians(fov);
        float tanHalfFOV = (float) Math.tan(radians / 2.0f);
        float top = near * tanHalfFOV;
        float right = top * aspectRatio;
        result[0][0] = near / right;
        result[1][1] = near / top;
        result[2][2] = -(far + near) / (far - near);
        result[2][3] = -(2.0f * far * near) / (far - near);
        result[3][2] = -1.0f;
        return new Matrix4f(result);
    }

    /**
     * Creates a scaling matrix
     *
     * @param x Scale on the x axis
     * @param y Scale on the y axis
     * @param z Scale on the z axis
     * @return Matrix4f
     */
    public static Matrix4f scale(float x, float y, float z) {
        float[][] result = new float[4][4];
        result[0][0] = x;
        result[1][1] = y;
        result[2][2] = z;
        result[3][3] = 1.0f;
        return new Matrix4f(result);
    }

    /**
     * Creates a rotation matrix around the x axis
     *
     * @param angle Angle in degrees
     * @return Matrix4f
     */
    public static Matrix4f rotateX(float angle) {
        float[][] result = new float[4][4];
        float radians = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        result[0][0] = 1.0f;
        result[1][1] = cos;
        result[1][2] = -sin;
        result[2][1] = sin;
        result[2][2] = cos;
        result[3][3] = 1.0f;
        return new Matrix4f(result);
    }

    /**
     * Creates a rotation matrix around the y axis
     *
     * @param angle Angle in degrees
     * @return Matrix4f
     */
    public static Matrix4f rotateY(float angle) {
        float[][] result = new float[4][4];
        float radians = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        result[0][0] = cos;
        result[0][2] = sin;
        result[1][1] = 1.0f;
        result[2][0] = -sin;
        result[2][2] = cos;
        result[3][3] = 1.0f;
        return new Matrix4f(result);
    }

    /**
     * Creates a rotation matrix around the z axis
     *
     * @param angle Angle in degrees
     * @return Matrix4f
     */
    public static Matrix4f rotateZ(float angle) {
        float[][] result = new float[4][4];
        float radians = (float) Math.toRadians(angle);
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        result[0][0] = cos;
        result[0][1] = -sin;
        result[1][0] = sin;
        result[1][1] = cos;
        result[2][2] = 1.0f;
        result[3][3] = 1.0f;
        return new Matrix4f(result);
    }

    /**
     * Creates a translation matrix
     *
     * @param x Translation on the x axis
     * @param y Translation on the y axis
     * @param z Translation on the z axis
     * @return Matrix4f
     */
    public static Matrix4f translate(float x, float y, float z) {
        float[][] result = new float[4][4];
        result[0][0] = 1.0f;
        result[1][1] = 1.0f;
        result[2][2] = 1.0f;
        result[3][3] = 1.0f;
        result[0][3] = x;
        result[1][3] = y;
        result[2][3] = z;
        return new Matrix4f(result);
    }

    public static Matrix4f identity() {
        float[][] result = new float[4][4];
        result[0][0] = 1.0f;
        result[1][1] = 1.0f;
        result[2][2] = 1.0f;
        result[3][3] = 1.0f;
        return new Matrix4f(result);
    }
}
