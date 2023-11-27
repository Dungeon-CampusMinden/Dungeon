package core.gui.util;

import core.utils.math.Vector2i;

import org.lwjgl.glfw.GLFW;

public class GUIUtils {

    /**
     * Gets the mouse position relative to the frame.
     *
     * @return Mouse position
     */
    public static Vector2i getMousePosition() {
        return getMousePosition(true);
    }

    /**
     * Gets the mouse position relative to the frame.
     *
     * @param flipY Whether to flip the y coordinate (true = flip, false = don't flip)
     * @return Mouse position
     */
    public static Vector2i getMousePosition(boolean flipY) {
        double[] x = new double[1];
        double[] y = new double[1];
        GLFW.glfwGetCursorPos(GLFW.glfwGetCurrentContext(), x, y);
        if (flipY) {
            Vector2i frameSize = getFrameSize();
            return new Vector2i((int) Math.round(x[0]), frameSize.y() - (int) Math.round(y[0]));
        }
        return new Vector2i((int) Math.round(x[0]), (int) Math.round(y[0]));
    }

    /**
     * Gets the size of the frame. (The size of the window in pixels)
     *
     * @return Frame size
     */
    public static Vector2i getFrameSize() {
        int[] width = new int[1];
        int[] height = new int[1];
        GLFW.glfwGetFramebufferSize(GLFW.glfwGetCurrentContext(), width, height);
        return new Vector2i(width[0], height[0]);
    }
}
