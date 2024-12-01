package de.fwatermann.dungine.graphics.scene.light;

import java.nio.ByteBuffer;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

/**
 * The `Light` class is an abstract base class for different types of lights in a 3D scene. It
 * provides common properties and methods for handling light attributes such as position, intensity,
 * direction, color, etc.
 *
 * @param <T> The type of the light extending this class.
 */
public abstract class Light<T extends Light<?>> {

  /**
   * Max number of lights supported by all GPUs
   *
   * <p>16KB is guaranteed to be supported by all GPUs! 16000 / 80 = 200 -> 200 - 1 for the actual
   * size
   */
  public static final int MAX_NUMBER_LIGHTS = 240;

  /** Size of the light structure in bytes. */
  public static final int STRUCT_SIZE = 64;

  private ByteBuffer buffer = null;

  /** The position of the light. */
  protected final Vector3f position = new Vector3f(0);

  /** The intensity of the light. */
  protected float intensity;

  /** The direction of the light. */
  protected final Vector3f direction = new Vector3f(0, 0, -1);

  /** The constant attenuation factor of the light. */
  protected float constant;

  /** The color of the light. */
  protected final Vector3f color = new Vector3f(1.0f);

  /** The linear attenuation factor of the light. */
  protected float linear;

  /** The exponent attenuation factor of the light. */
  protected float exponent;

  /** The cutoff distance of the light. */
  protected float cutOff;

  /** The cutoff angle of the light. */
  protected float cutOfAngle;

  /** The type of the light. */
  protected LightType type;

  /**
   * Constructs a `Light` with the specified type.
   *
   * @param type The type of the light.
   */
  protected Light(LightType type) {
    this.type = type;
  }

  private void initBuffer() {
    this.buffer = BufferUtils.createByteBuffer(64);
    this.buffer
        .putFloat(this.position.x)
        .putFloat(this.position.y)
        .putFloat(this.position.z) // 0
        .putFloat(this.intensity) // 3
        .putFloat(this.direction.x)
        .putFloat(this.direction.y)
        .putFloat(this.direction.z) // 4
        .putFloat(this.constant) // 7
        .putFloat(this.color.x)
        .putFloat(this.color.y)
        .putFloat(this.color.z) // 8
        .putFloat(this.linear) // 11
        .putFloat(this.exponent) // 12
        .putFloat(this.cutOff) // 13
        .putFloat(this.cutOfAngle) // 14
        .putInt(this.type.id()); // 15
    this.buffer.position(0);
  }

  /**
   * Returns the light structure as a `ByteBuffer`.
   *
   * @return The light structure.
   */
  public final ByteBuffer getStruct() {
    if (this.buffer == null) {
      this.initBuffer();
    }
    return this.buffer.position(0);
  }

  /**
   * Sets the position of the light.
   *
   * @param position The position to set.
   * @return The updated light instance.
   */
  protected T position(Vector3f position) {
    this.position.set(position);
    if (this.buffer != null) {
      this.buffer
          .position(0)
          .putFloat(this.position.x)
          .putFloat(this.position.y)
          .putFloat(this.position.z);
    }
    return (T) this;
  }

  /**
   * Gets the position of the light.
   *
   * @return The position of the light.
   */
  protected Vector3f position() {
    return this.position;
  }

  /**
   * Sets the intensity of the light.
   *
   * @param intensity The intensity to set.
   * @return The updated light instance.
   */
  protected T intensity(float intensity) {
    this.intensity = intensity;
    if (this.buffer != null) {
      this.buffer.position(3 * 4).putFloat(this.intensity);
    }
    return (T) this;
  }

  /**
   * Gets the intensity of the light.
   *
   * @return The intensity of the light.
   */
  protected float intensity() {
    return this.intensity;
  }

  /**
   * Sets the direction of the light.
   *
   * @param direction The direction to set.
   * @return The updated light instance.
   */
  protected T direction(Vector3f direction) {
    this.direction.set(direction);
    if (this.buffer != null) {
      this.buffer
          .position(4 * 4)
          .putFloat(this.direction.x)
          .putFloat(this.direction.y)
          .putFloat(this.direction.z);
    }
    return (T) this;
  }

  /**
   * Gets the direction of the light.
   *
   * @return The direction of the light.
   */
  protected Vector3f direction() {
    return this.direction;
  }

  /**
   * Sets the constant attenuation factor of the light.
   *
   * @param constant The constant attenuation factor to set.
   * @return The updated light instance.
   */
  protected T constant(float constant) {
    this.constant = constant;
    if (this.buffer != null) {
      this.buffer.position(7 * 4).putFloat(this.constant);
    }
    return (T) this;
  }

  /**
   * Gets the constant attenuation factor of the light.
   *
   * @return The constant attenuation factor of the light.
   */
  protected float constant() {
    return this.constant;
  }

  /**
   * Sets the color of the light.
   *
   * @param color The color to set.
   * @return The updated light instance.
   */
  protected T color(Vector3f color) {
    this.color.set(color);
    if (this.buffer != null) {
      this.buffer
          .position(8 * 4)
          .putFloat(this.color.x)
          .putFloat(this.color.y)
          .putFloat(this.color.z);
    }
    return (T) this;
  }

  /**
   * Gets the color of the light.
   *
   * @return The color of the light.
   */
  protected Vector3f color() {
    return this.color;
  }

  /**
   * Sets the linear attenuation factor of the light.
   *
   * @param linear The linear attenuation factor to set.
   * @return The updated light instance.
   */
  protected T linear(float linear) {
    this.linear = linear;
    if (this.buffer != null) {
      this.buffer.position(11 * 4).putFloat(this.linear);
    }
    return (T) this;
  }

  /**
   * Gets the linear attenuation factor of the light.
   *
   * @return The linear attenuation factor of the light.
   */
  protected float linear() {
    return this.linear;
  }

  /**
   * Sets the exponent attenuation factor of the light.
   *
   * @param exponent The exponent attenuation factor to set.
   * @return The updated light instance.
   */
  protected T exponent(float exponent) {
    this.exponent = exponent;
    if (this.buffer != null) {
      this.buffer.position(12 * 4).putFloat(this.exponent);
    }
    return (T) this;
  }

  /**
   * Gets the exponent attenuation factor of the light.
   *
   * @return The exponent attenuation factor of the light.
   */
  protected float exponent() {
    return this.exponent;
  }

  /**
   * Sets the cutoff distance of the light.
   *
   * @param cutOff The cutoff distance to set.
   * @return The updated light instance.
   */
  protected T cutOff(float cutOff) {
    this.cutOff = cutOff;
    if (this.buffer != null) {
      this.buffer.position(13 * 4).putFloat(this.cutOff);
    }
    return (T) this;
  }

  /**
   * Gets the cutoff distance of the light.
   *
   * @return The cutoff distance of the light.
   */
  protected float cutOff() {
    return this.cutOff;
  }

  /**
   * Sets the cutoff angle of the light.
   *
   * @param cutOfAngle The cutoff angle to set.
   * @return The updated light instance.
   */
  protected T cutOfAngle(float cutOfAngle) {
    this.cutOfAngle = cutOfAngle;
    if (this.buffer != null) {
      this.buffer.position(14 * 4).putFloat(this.cutOfAngle);
    }
    return (T) this;
  }

  /**
   * Gets the cutoff angle of the light.
   *
   * @return The cutoff angle of the light.
   */
  protected float cutOfAngle() {
    return this.cutOfAngle;
  }

  /**
   * Gets the type of the light.
   *
   * @return The type of the light.
   */
  public final LightType type() {
    return this.type;
  }
}
