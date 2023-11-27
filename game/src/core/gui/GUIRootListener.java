package core.gui;

import core.gui.events.GUIMouseClickEvent;
import core.gui.events.GUIMouseMoveEvent;
import core.gui.events.GUIScrollEvent;
import core.gui.util.GUIUtils;
import core.utils.math.Vector2i;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;

public class GUIRootListener {

    private static Vector2i lastMousePos = new Vector2i(0, 0);
    private static GLFWCursorPosCallbackI cursorPosCallbackI;
    private static GLFWMouseButtonCallbackI mouseButtonCallbackI;
    private static GLFWScrollCallbackI scrollCallbackI;

    protected static void init(GUIRoot root) {
        initMouseMove(root);
        initMouseClick(root);
        initMouseScroll(root);
    }

    private static void initMouseMove(GUIRoot root) {
        cursorPosCallbackI =
                GLFW.glfwSetCursorPosCallback(
                        GLFW.glfwGetCurrentContext(),
                        (window, xpos, ypos) -> {
                            if (cursorPosCallbackI != null)
                                cursorPosCallbackI.invoke(window, xpos, ypos);
                            Vector2i mousePos = new Vector2i((int) xpos, (int) ypos);
                            root.event(new GUIMouseMoveEvent(lastMousePos, mousePos));
                            lastMousePos = mousePos;
                        });
    }

    private static void initMouseClick(GUIRoot root) {
        mouseButtonCallbackI =
                GLFW.glfwSetMouseButtonCallback(
                        GLFW.glfwGetCurrentContext(),
                        (window, button, action, mods) -> {
                            if (mouseButtonCallbackI != null)
                                mouseButtonCallbackI.invoke(window, button, action, mods);
                            root.event(
                                    new GUIMouseClickEvent(
                                            button, action, mods, GUIUtils.getMousePosition()));
                        });
    }

    private static void initMouseScroll(GUIRoot root) {
        scrollCallbackI =
                GLFW.glfwSetScrollCallback(
                        GLFW.glfwGetCurrentContext(),
                        (window, xoffset, yoffset) -> {
                            if (scrollCallbackI != null)
                                scrollCallbackI.invoke(window, xoffset, yoffset);
                            Vector2i scroll =
                                    new Vector2i(
                                            (int) Math.round(xoffset), (int) Math.round(yoffset));
                            Vector2i mousePos = GUIUtils.getMousePosition();
                            root.event(new GUIScrollEvent(scroll, mousePos));
                        });
    }
}
