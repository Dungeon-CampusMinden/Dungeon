package core.gui.backend.opengl;

import core.utils.logging.CustomLogLevel;

import org.lwjgl.opengl.GL33;

import java.util.HashMap;

public class OpenGLRenderStructure {

    private final HashMap<String, Integer> uniformLocations = new HashMap<>();

    public int vao, vbo, ebo, frameBuffer, texture, shader;

    public OpenGLRenderStructure() {
        this.vao = 0;
        this.vbo = 0;
        this.ebo = 0;
        this.frameBuffer = 0;
        this.texture = 0;
        this.shader = 0;
    }

    public int getUniformLocation(String name) {
        if (this.uniformLocations.containsKey(name)) {
            return this.uniformLocations.get(name);
        } else {
            int location = GL33.glGetUniformLocation(this.shader, name);
            this.uniformLocations.put(name, location);
            if (location == -1) {
                OpenGLUtil.log(
                        CustomLogLevel.WARNING,
                        "Uniform '%s' not found in shader program %d\n",
                        name,
                        this.shader);
            } else {
                OpenGLUtil.log(
                        CustomLogLevel.DEBUG,
                        "Found uniform '%s' in shader program %d@%d\n",
                        name,
                        this.shader,
                        location);
            }
            return location;
        }
    }
}
