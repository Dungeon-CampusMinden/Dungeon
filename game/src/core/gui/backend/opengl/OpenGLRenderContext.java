package core.gui.backend.opengl;

import core.gui.util.Logging;
import core.utils.logging.CustomLogLevel;

import org.lwjgl.opengl.GL33;

import java.util.HashMap;
import java.util.Map;

public class OpenGLRenderContext {

    private final HashMap<String, Integer> uniformLocations = new HashMap<>();
    private boolean begun = false;
    private boolean begunStencil = false;

    public int vao, vbo, ebo, frameBuffer, renderBuffer, texture, shader;
    public Map<String, Integer> additionalBuffers = new HashMap<>();

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
            Logging.log(CustomLogLevel.WARNING, "OpenGLRenderStructure.begin() called twice!");
        }
        GL33.glBindVertexArray(this.vao);
        GL33.glUseProgram(this.shader);
        this.begun = true;
    }

    public void beginStencil() {
        GL33.glEnable(GL33.GL_STENCIL_TEST);
        GL33.glClearStencil(0);
        GL33.glClear(GL33.GL_STENCIL_BUFFER_BIT);
        this.begunStencil = true;
    }

    public void writeStencil() {
        if (!this.begunStencil) {
            Logging.log(
                    CustomLogLevel.WARNING,
                    "OpenGLRenderStructure.writeStencil() called before .beginStencil()!");
        }
        GL33.glStencilMask(0xFF);
        GL33.glStencilFunc(GL33.GL_ALWAYS, 1, 0xFF);
        GL33.glStencilOp(GL33.GL_KEEP, GL33.GL_KEEP, GL33.GL_REPLACE);
    }

    public void useStencil() {
        if (!this.begunStencil) {
            Logging.log(
                    CustomLogLevel.WARNING,
                    "OpenGLRenderStructure.useStencil() called before .beginStencil()!");
        }
        GL33.glStencilMask(0x00);
        GL33.glStencilFunc(GL33.GL_EQUAL, 1, 0xFF);
        GL33.glStencilOp(GL33.GL_KEEP, GL33.GL_KEEP, GL33.GL_KEEP);
    }

    public void end() {
        if (this.begun) {
            GL33.glUseProgram(0);
            GL33.glBindVertexArray(0);
            this.begun = false;
        } else {
            Logging.log(
                    CustomLogLevel.WARNING, "OpenGLRenderStructure.end() called before .begin()!");
        }
    }

    public void endStencil() {
        if (!this.begunStencil) {
            Logging.log(
                    CustomLogLevel.WARNING,
                    "OpenGLRenderStructure.endStencil() called before .beginStencil()!");
        }
        GL33.glDisable(GL33.GL_STENCIL_TEST);
    }

    public void draw() {
        if (this.begun) {
            GL33.glDrawElements(GL33.GL_TRIANGLE_STRIP, 4, GL33.GL_UNSIGNED_SHORT, 0);
        } else {
            Logging.log(
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
                Logging.log(
                        CustomLogLevel.WARNING,
                        "Uniform '%s' not found in shader program %d\n",
                        name,
                        this.shader);
            } else {
                Logging.log(
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
