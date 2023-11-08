package core.gui.backend.opengl;

import core.utils.logging.CustomLogLevel;

import org.lwjgl.opengl.GL33;

import java.util.HashMap;

public class OpenGLRenderContext {

    private final HashMap<String, Integer> uniformLocations = new HashMap<>();
    private boolean begun = false;

    public int vao, vbo, ebo, frameBuffer, texture, shader;

    public OpenGLRenderContext() {
        this.vao = 0;
        this.vbo = 0;
        this.ebo = 0;
        this.frameBuffer = 0;
        this.texture = 0;
        this.shader = 0;
    }

    public void begin() {
        if (this.begun) {
            OpenGLUtil.log(CustomLogLevel.WARNING, "OpenGLRenderStructure.begin() called twice!");
        }
        GL33.glBindVertexArray(this.vao);
        GL33.glUseProgram(this.shader);
        this.begun = true;
    }

    public void end() {
        if (this.begun) {
            GL33.glUseProgram(0);
            GL33.glBindVertexArray(0);
            this.begun = false;
        } else {
            OpenGLUtil.log(
                    CustomLogLevel.WARNING, "OpenGLRenderStructure.end() called before .begin()!");
        }
    }

    public void draw() {
        if (this.begun) {
            GL33.glDrawElements(GL33.GL_TRIANGLE_STRIP, 4, GL33.GL_UNSIGNED_SHORT, 0);
        } else {
            OpenGLUtil.log(
                    CustomLogLevel.WARNING,
                    "OpenGLRenderStructure.draw() called before .begin()! Not drawing.");
        }
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
