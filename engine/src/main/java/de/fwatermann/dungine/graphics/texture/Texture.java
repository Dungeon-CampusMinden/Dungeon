package de.fwatermann.dungine.graphics.texture;

import de.fwatermann.dungine.utils.Disposable;
import de.fwatermann.dungine.utils.GLUtils;
import de.fwatermann.dungine.utils.ThreadUtils;
import java.nio.ByteBuffer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL33;

/**
 * The Texture class represents a texture in OpenGL. It provides methods to bind and unbind the
 * texture, as well as getters and setters for various texture properties.
 */
public class Texture implements Disposable {

  private static final Logger LOGGER = LogManager.getLogger(Texture.class);

  private final int glTextureId;
  private int unit;
  private int width;
  private int height;
  private int minFilter;
  private int maxFilter;
  private int wrapS;
  private int wrapT;

  /**
   * Constructs a new Texture with the specified parameters.
   *
   * @param width the width of the texture
   * @param height the height of the texture
   * @param format the format of the texture
   * @param minFilter the minifying filter of the texture
   * @param maxFilter the magnification filter of the texture
   * @param wrapS the wrap parameter for texture coordinate s
   * @param wrapT the wrap parameter for texture coordinate t
   * @param pixels the pixel data of the texture
   */
  public Texture(
      int width,
      int height,
      int format,
      int minFilter,
      int maxFilter,
      int wrapS,
      int wrapT,
      ByteBuffer pixels) {
    ThreadUtils.checkMainThread();
    this.glTextureId = GL33.glGenTextures();
    this.width = width;
    this.height = height;
    GL33.glActiveTexture(GL33.GL_TEXTURE0);
    GL33.glBindTexture(GL33.GL_TEXTURE_2D, this.glTextureId);
    if (pixels != null) {
      GL33.glTexImage2D(
          GL33.GL_TEXTURE_2D, 0, format, width, height, 0, format, GL33.GL_UNSIGNED_BYTE, pixels);
    } else {
      GL33.glTexImage2D(
          GL33.GL_TEXTURE_2D, 0, format, width, height, 0, format, GL33.GL_UNSIGNED_BYTE, 0);
    }
    GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, minFilter);
    GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, maxFilter);
    GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, wrapS);
    GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, wrapT);
    GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
    LOGGER.debug("Created texture {}", this.glTextureId);
  }

  /**
   * Constructs a new Texture with the specified parameters and default values for minFilter,
   * maxFilter, wrapS, and wrapT.
   *
   * @param width the width of the texture
   * @param height the height of the texture
   * @param format the format of the texture
   * @param pixels the pixel data of the texture
   */
  public Texture(int width, int height, int format, ByteBuffer pixels) {
    this(
        width,
        height,
        format,
        GL33.GL_NEAREST,
        GL33.GL_NEAREST,
        GL33.GL_CLAMP_TO_EDGE,
        GL33.GL_CLAMP_TO_EDGE,
        pixels);
  }

  /**
   * Constructs a new Texture with the specified parameters and default values for format,
   * minFilter, maxFilter, wrapS, and wrapT.
   *
   * @param width the width of the texture
   * @param height the height of the texture
   * @param pixels the pixel data of the texture
   */
  public Texture(int width, int height, ByteBuffer pixels) {
    this(width, height, GL33.GL_RGBA, pixels);
  }

  /**
   * Constructs a new Texture with the specified width and height and default values for format,
   *
   * @param width the width of the texture
   * @param height the height of the texture
   */
  public Texture(int width, int height) {
    this(width, height, GL33.GL_RGBA, null);
  }

  /** Binds this texture to the current texture unit. */
  public void bind() {
    this.unit = GL33.glGetInteger(GL33.GL_ACTIVE_TEXTURE);
    this.bind(this.unit);
  }

  /**
   * Binds this texture to the specified texture unit.
   *
   * @param textureUnit the texture unit to bind this texture to
   */
  public void bind(int textureUnit) {
    ThreadUtils.checkMainThread();
    this.unit = textureUnit;
    GL33.glActiveTexture(textureUnit);
    GL33.glBindTexture(GL33.GL_TEXTURE_2D, this.glTextureId);
    if (this.unit != textureUnit)
      LOGGER.trace(
          "Bound texture {} to unit {} ({})",
          this.glTextureId,
          textureUnit,
          (textureUnit - GL33.GL_TEXTURE0));
  }

  /** Unbinds this texture from the current texture unit. */
  public void unbind() {
    ThreadUtils.checkMainThread();
    int[] currentUnit = new int[1];
    GL33.glGetIntegerv(GL33.GL_ACTIVE_TEXTURE, currentUnit);
    if (currentUnit[0] != this.unit) {
      GL33.glActiveTexture(this.unit);
      GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
      GL33.glActiveTexture(currentUnit[0]);
    } else {
      GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
    }
    LOGGER.trace("Unbound texture {}", this.glTextureId);
  }

  /**
   * Returns the width of this texture.
   *
   * @return the width of this texture
   */
  public int width() {
    return this.width;
  }

  /**
   * Returns the height of this texture.
   *
   * @return the height of this texture
   */
  public int height() {
    return this.height;
  }

  /**
   * Returns the minifying filter of this texture.
   *
   * @return the minifying filter of this texture
   */
  public int minFilter() {
    return this.minFilter;
  }

  /**
   * Sets the minifying filter of this texture and returns this texture.
   *
   * @param minFilter the minifying filter to set
   * @return this texture
   */
  public Texture minFilter(int minFilter) {
    this.minFilter = minFilter;
    GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, minFilter);
    return this;
  }

  /**
   * Returns the magnification filter of this texture.
   *
   * @return the magnification filter of this texture
   */
  public int maxFilter() {
    return this.maxFilter;
  }

  /**
   * Sets the magnification filter of this texture and returns this texture.
   *
   * @param maxFilter the magnification filter to set
   * @return this texture
   */
  public Texture maxFilter(int maxFilter) {
    this.maxFilter = maxFilter;
    GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, maxFilter);
    return this;
  }

  /**
   * Returns the wrap parameter for texture coordinate s of this texture.
   *
   * @return the wrap parameter for texture coordinate s of this texture
   */
  public int wrapS() {
    return this.wrapS;
  }

  /**
   * Sets the wrap parameter for texture coordinate s of this texture and returns this texture.
   *
   * @param wrapS the wrap parameter for texture coordinate s to set
   * @return this texture
   */
  public Texture wrapS(int wrapS) {
    this.wrapS = wrapS;
    GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, wrapS);
    return this;
  }

  /**
   * Returns the wrap parameter for texture coordinate t of this texture.
   *
   * @return the wrap parameter for texture coordinate t of this texture
   */
  public int wrapT() {
    return this.wrapT;
  }

  /**
   * Sets the wrap parameter for texture coordinate t of this texture and returns this texture.
   *
   * @param wrapT the wrap parameter for texture coordinate t to set
   * @return this texture
   */
  public Texture wrapT(int wrapT) {
    this.wrapT = wrapT;
    GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, wrapT);
    return this;
  }

  /**
   * Get the pixels in the texture.
   *
   * <p>Note: The pixels are fetched from the GPU so this method should be used sparingly.
   *
   * @return the pixel data of this texture
   */
  public ByteBuffer readPixels() {
    ThreadUtils.checkMainThread();
    ByteBuffer buffer = BufferUtils.createByteBuffer(this.width * this.height * 4);
    int currentTexture = GL33.glGetInteger(GL33.GL_TEXTURE_BINDING_2D);
    GL33.glBindTexture(GL33.GL_TEXTURE_2D, this.glTextureId);
    GL33.glGetTexImage(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, buffer);
    GL33.glBindTexture(GL33.GL_TEXTURE_2D, currentTexture);
    return buffer;
  }

  /**
   * Reads the pixels of this texture into the specified destination buffer.
   *
   * <p>Note: The destination buffer must be direct and in native byte order. The destination buffer
   * must also fit the dimensions of the texture (width * height * 4 (RGBA))!
   *
   * <p>Note: The pixels are fetched from the GPU so this method should be used sparingly.
   *
   * @param destination the destination buffer to read the pixels into
   * @return this texture
   */
  public Texture readPixels(ByteBuffer destination) {
    ThreadUtils.checkMainThread();
    GLUtils.checkBuffer(destination);
    if (destination.remaining() < this.width * this.height * 4) {
      throw new IllegalArgumentException(
          "Destination buffer does not fit to the dimensions. The remaining length of the buffer must be width * height * 4 (RGBA)!");
    }
    int currentTexture = GL33.glGetInteger(GL33.GL_TEXTURE_BINDING_2D);
    GL33.glBindTexture(GL33.GL_TEXTURE_2D, this.glTextureId);
    GL33.glGetTexImage(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, destination);
    GL33.glBindTexture(GL33.GL_TEXTURE_2D, currentTexture);
    return this;
  }

  /**
   * Sets the pixel data of this texture and uploads it to the GPU.
   *
   * <p>Note: The pixel buffer must be direct and in native byte order. The pixel buffer must also
   * fit the dimensions of the texture (width * height * 4 (RGBA))!
   *
   * @param pixels the pixel data to set
   * @return this texture
   */
  public Texture pixels(ByteBuffer pixels) {
    return this.pixels(this.width, this.height, pixels);
  }

  /**
   * Sets the pixel data of this texture with the specified dimensions and uploads it to the GPU.
   *
   * <p>Note: The pixel buffer must be direct and in native byte order. The pixel buffer must also
   * fit the dimensions of the texture (width * height * 4 (RGBA))!
   *
   * @param width the width of the pixel data
   * @param height the height of the pixel data
   * @param pixels the pixel data to set
   * @return this texture
   */
  public Texture pixels(int width, int height, ByteBuffer pixels) {
    GLUtils.checkBuffer(pixels);
    if (pixels.limit() < width * height * 4) {
      throw new IllegalArgumentException(
          "Pixel buffer does not fit to the dimensions. The length of the buffer must be width * height * 4 (RGBA)!");
    }
    this.width = width;
    this.height = height;
    int currentTexture = GL33.glGetInteger(GL33.GL_TEXTURE_BINDING_2D);
    GL33.glBindTexture(GL33.GL_TEXTURE_2D, this.glTextureId);
    GL33.glTexImage2D(
        GL33.GL_TEXTURE_2D,
        0,
        GL33.GL_RGBA,
        this.width,
        this.height,
        0,
        GL33.GL_RGBA,
        GL33.GL_UNSIGNED_BYTE,
        pixels);
    GL33.glBindTexture(GL33.GL_TEXTURE_2D, currentTexture);
    return this;
  }

  /** Disposes this texture by deleting the OpenGL texture object. */
  @Override
  public void dispose() {
    GL33.glDeleteTextures(this.glTextureId);
  }

  public int glHandle() {
    return this.glTextureId;
  }
}
