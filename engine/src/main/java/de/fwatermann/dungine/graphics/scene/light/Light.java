package de.fwatermann.dungine.graphics.scene.light;

import java.nio.ByteBuffer;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

public abstract class Light<T extends Light<?>> {

  /**
   * 16KB is guaranteed to be supported by all GPUs! 16000 / 80 = 200 -> 200 - 1 for the actual size
   */
  public static final int MAX_NUMBER_LIGHTS = 240;

  public static final int STRUCT_SIZE = 64;

  private ByteBuffer buffer = null;
  protected final Vector3f position = new Vector3f(0);
  protected float intensity;
  protected final Vector3f direction = new Vector3f(0, 0, -1);
  protected float constant;
  protected final Vector3f color = new Vector3f(1.0f);
  protected float linear;
  protected float exponent;
  protected float cutOff;
  protected float cutOfAngle;
  protected LightType type;

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

  public final ByteBuffer getStruct() {
    if (this.buffer == null) {
      this.initBuffer();
    }
    return this.buffer.position(0);
  }

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

  protected Vector3f position() {
    return this.position;
  }

  protected T intensity(float intensity) {
    this.intensity = intensity;
    if (this.buffer != null) {
      this.buffer.position(3 * 4).putFloat(this.intensity);
    }
    return (T) this;
  }

  protected float intensity() {
    return this.intensity;
  }

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

  protected Vector3f direction() {
    return this.direction;
  }

  protected T constant(float constant) {
    this.constant = constant;
    if (this.buffer != null) {
      this.buffer.position(7 * 4).putFloat(this.constant);
    }
    return (T) this;
  }

  protected float constant() {
    return this.constant;
  }

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

  protected Vector3f color() {
    return this.color;
  }

  protected T linear(float linear) {
    this.linear = linear;
    if (this.buffer != null) {
      this.buffer.position(11 * 4).putFloat(this.linear);
    }
    return (T) this;
  }

  protected float linear() {
    return this.linear;
  }

  protected T exponent(float exponent) {
    this.exponent = exponent;
    if (this.buffer != null) {
      this.buffer.position(12 * 4).putFloat(this.exponent);
    }
    return (T) this;
  }

  protected float exponent() {
    return this.exponent;
  }

  protected T cutOff(float cutOff) {
    this.cutOff = cutOff;
    if (this.buffer != null) {
      this.buffer.position(13 * 4).putFloat(this.cutOff);
    }
    return (T) this;
  }

  protected float cutOff() {
    return this.cutOff;
  }

  protected T cutOfAngle(float cutOfAngle) {
    this.cutOfAngle = cutOfAngle;
    if (this.buffer != null) {
      this.buffer.position(14 * 4).putFloat(this.cutOfAngle);
    }
    return (T) this;
  }

  protected float cutOfAngle() {
    return this.cutOfAngle;
  }

  public final LightType type() {
    return this.type;
  }
}
