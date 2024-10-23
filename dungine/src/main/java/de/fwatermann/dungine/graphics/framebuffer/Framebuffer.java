package de.fwatermann.dungine.graphics.framebuffer;

import de.fwatermann.dungine.event.EventHandler;
import de.fwatermann.dungine.event.EventListener;
import de.fwatermann.dungine.event.EventManager;
import de.fwatermann.dungine.event.window.WindowResizeEvent;
import de.fwatermann.dungine.exception.OpenGLException;
import de.fwatermann.dungine.graphics.texture.Texture;
import de.fwatermann.dungine.utils.Disposable;
import de.fwatermann.dungine.utils.GLUtils;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL33;

/**
 * Represents a framebuffer, a collection of buffers that can be used as the destination for
 * rendering. This class provides functionality to manage a framebuffer, including its creation,
 * resizing, and the management of attached textures. It also listens to window resize events to
 * automatically adjust its size if auto-resizing is enabled.
 */
public class Framebuffer implements Disposable, EventListener {

  private static final Logger LOGGER = LogManager.getLogger(Framebuffer.class);
  private static final Set<Framebuffer> framebufferInstances = new HashSet<>();

  static {
    EventManager.getInstance().registerStaticListener(Framebuffer.class);
  }

  @EventHandler
  private static void onResize(WindowResizeEvent event) {
    if (event.isCanceled()) return;
    framebufferInstances.forEach(
        f -> {
          f.width = event.to.x;
          f.height = event.to.y;
        });
  }

  private int glHandle;
  private int glDepthBuffer;
  private int width, height;
  private boolean autoResize = true;

  private final Map<Integer, Texture> attachedTextures = new HashMap<>();

  /**
   * Constructs a new Framebuffer with the specified width and height. The autoResize parameter
   * determines whether the framebuffer should automatically resize its attached textures when its
   * dimensions change.
   *
   * @param width the width of the framebuffer
   * @param height the height of the framebuffer
   * @param autoResize whether the framebuffer should automatically resize its attached textures.
   */
  public Framebuffer(int width, int height, boolean autoResize) {
    framebufferInstances.add(this);
    this.width = width;
    this.height = height;
    this.autoResize = autoResize;
    this.initGL();
  }

  private void initGL() {
    this.glHandle = GL33.glGenFramebuffers();
    int currentFramebuffer = GL33.glGetInteger(GL33.GL_DRAW_FRAMEBUFFER_BINDING);
    GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, this.glHandle);

    this.glDepthBuffer = GL33.glGenRenderbuffers();
    GL33.glBindRenderbuffer(GL33.GL_RENDER, this.glDepthBuffer);
    GL33.glRenderbufferStorage(
        GL33.GL_RENDERBUFFER, GL33.GL_DEPTH24_STENCIL8, this.width, this.height);
    GL33.glFramebufferRenderbuffer(
        GL33.GL_FRAMEBUFFER,
        GL33.GL_DEPTH_STENCIL_ATTACHMENT,
        GL33.GL_RENDERBUFFER,
        this.glDepthBuffer);
    GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, 0);

    this.checkComplete();
    GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, currentFramebuffer);
  }

  /**
   * Returns the current width of the framebuffer.
   *
   * @return The width of the framebuffer in pixels.
   */
  public int width() {
    return this.width;
  }

  /**
   * Sets the width of the framebuffer and updates its size. This method allows for chaining by
   * returning the Framebuffer instance.
   *
   * @param width The new width to set for the framebuffer.
   * @return The Framebuffer instance for chaining.
   */
  public Framebuffer width(int width) {
    this.width = width;
    this.updateFramebufferSizes();
    return this;
  }

  /**
   * Returns the current height of the framebuffer.
   *
   * @return The height of the framebuffer in pixels.
   */
  public int height() {
    return this.height;
  }

  /**
   * Sets the height of the framebuffer and updates its size. This method allows for chaining by
   * returning the Framebuffer instance.
   *
   * @param height The new height to set for the framebuffer.
   * @return The Framebuffer instance for chaining.
   */
  public Framebuffer height(int height) {
    this.height = height;
    this.updateFramebufferSizes();
    return this;
  }

  /**
   * Returns the OpenGL handle of the framebuffer.
   *
   * @return The OpenGL handle identifier for the framebuffer.
   */
  public int glHandle() {
    return this.glHandle;
  }

  /** Binds this framebuffer as the current OpenGL framebuffer. */
  public void bind() {
    GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, this.glHandle);
  }

  /**
   * Binds this framebuffer as the current OpenGL framebuffer and sets the viewport to the specified
   * dimensions.
   *
   * @param x X coordinate of the viewport.
   * @param y Y coordinate of the viewport.
   * @param width Width of the viewport.
   * @param height Height of the viewport.
   */
  public void bind(int x, int y, int width, int height) {
    GL33.glViewport(x, y, width, height);
    GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, this.glHandle);
  }

  /**
   * Unbinds the current OpenGL framebuffer, effectively setting the default framebuffer as active.
   */
  public void unbind() {
    GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
  }

  /**
   * Checks if this framebuffer is currently bound in OpenGL.
   *
   * @return true if this framebuffer is the current OpenGL framebuffer, false otherwise.
   */
  public boolean isBound() {
    return this.glHandle == GL33.glGetInteger(GL33.GL_FRAMEBUFFER_BINDING);
  }

  /**
   * Attaches a texture to this framebuffer at the specified attachment point.
   *
   * @param texture The texture to attach.
   * @param attachment The attachment point (e.g., GL33.GL_COLOR_ATTACHMENT0).
   */
  public void attachTexture(Texture texture, int attachment) {
    int currentFramebuffer = GL33.glGetInteger(GL33.GL_DRAW_FRAMEBUFFER_BINDING);
    GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, this.glHandle);
    GL33.glFramebufferTexture2D(
        GL33.GL_FRAMEBUFFER, attachment, GL33.GL_TEXTURE_2D, texture.glHandle(), 0);
    GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, currentFramebuffer);
    this.checkComplete();
    GLUtils.checkError();
    if (this.attachedTextures.containsKey(attachment)) {
      LOGGER.warn(
          "Overwriting attached texture at attachment {} on Framebuffer {}",
          attachment,
          this.glHandle);
    }
    this.attachedTextures.put(attachment, texture);
  }

  /**
   * Detaches the texture from the specified attachment point.
   *
   * @param attachment The attachment point from which to detach the texture.
   */
  public void detachTexture(int attachment) {
    int currentFramebuffer = GL33.glGetInteger(GL33.GL_DRAW_FRAMEBUFFER_BINDING);
    GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, this.glHandle);
    GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, attachment, GL33.GL_TEXTURE_2D, 0, 0);
    GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, currentFramebuffer);
    this.checkComplete();
    GLUtils.checkError();
    this.attachedTextures.remove(attachment);
  }

  private void checkComplete() {
    if (GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
      throw new OpenGLException("Framebuffer is not complete");
    }
  }

  private void updateFramebufferSizes() {
    GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, this.glDepthBuffer);
    GL33.glRenderbufferStorage(
        GL33.GL_RENDERBUFFER, GL33.GL_DEPTH_COMPONENT, this.width, this.height);
    GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, 0);

    if (this.autoResize) {
      this.attachedTextures
          .values()
          .forEach(
              t -> {
                GL33.glBindTexture(GL33.GL_TEXTURE_2D, t.glHandle());
                GL33.glTexImage2D(
                    GL33.GL_TEXTURE_2D,
                    0,
                    GL33.GL_RGBA,
                    this.width,
                    this.height,
                    0,
                    GL33.GL_RGBA,
                    GL33.GL_UNSIGNED_BYTE,
                    0);
                GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
              });
    }
  }

  @Override
  public void dispose() {}
}
