package de.fwatermann.dungine.utils;

import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;

import de.fwatermann.dungine.exception.GLFWException;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFWVidMode;

public class MonitorUtils {

    /**
     * Retrieves the resolution of the default monitor.
     *
     * <p>This method first gets the primary monitor using the glfwGetPrimaryMonitor() function. Then,
     * it retrieves the video mode of the primary monitor using the glfwGetVideoMode() function. If
     * the video mode is null, it throws a GLFWException indicating that the video mode of the primary
     * monitor could not be retrieved. If the video mode is not null, it creates a new Vector2i
     * instance with the width and height of the video mode and returns it.
     *
     * @return a Vector2i instance representing the resolution of the default monitor.
     * @throws GLFWException if the video mode of the primary monitor could not be retrieved.
     */
    public static Vector2i getDefaultMonitorResolution() {
        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode mode = glfwGetVideoMode(monitor);
        if (mode == null) {
            throw new GLFWException("Failed to get video mode of primary monitor");
        }
        return new Vector2i(mode.width(), mode.height());
    }
}
