package de.fwatermann.dungine.graphics.scene.light;


import java.nio.ByteBuffer;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

public abstract class Light<T extends Light<?>> {

  /**
   * 16KB is guaranteed to be supported by all GPUs! 16000 / 80 = 200 -> 200 - 1 for the actual size
   */
  public static final int MAX_NUMBER_LIGHTS = 199;

  private ByteBuffer buffer = null;
  protected final Vector3f position = new Vector3f(0);
  protected final Vector3f direction = new Vector3f(0, 0, -1);
  protected final Vector3f color = new Vector3f(1.0f);
  protected float intensity;
  protected float constant;
  protected float linear;
  protected float exponent;
  protected float cutOff;
  protected float cutOfAngle;
  protected LightType type;

  protected Light(LightType type) {
    this.type = type;
  }

  private void initBuffer() {
    this.buffer = BufferUtils.createByteBuffer(80);
    this.buffer.asFloatBuffer().position(0)
      .put(this.position.x).put(this.position.y).put(this.position.z).put(0)
      .put(this.direction.x).put(this.direction.y).put(this.direction.z).put(0)
      .put(this.color.x).put(this.color.y).put(this.color.z).put(0)
      .put(this.intensity)
      .put(this.constant)
      .put(this.linear)
      .put(this.exponent)
      .put(this.cutOff)
      .put(this.cutOfAngle);
    this.buffer.asIntBuffer().position(18).put(this.type.id());
    this.buffer.position(0).limit(80);
  }

  public final ByteBuffer getStruct() {
    if(this.buffer == null) {
      this.initBuffer();
    }
    return this.buffer;
  }

  protected T position(Vector3f position) {
    this.position.set(position);
    this.buffer.asFloatBuffer().position(0)
      .put(this.position.x).put(this.position.y).put(this.position.z).put(0);
    return (T) this;
  }

  protected Vector3f position() {
    return this.position;
  }

  protected T direction(Vector3f direction) {
    this.direction.set(direction);
    this.buffer.asFloatBuffer().position(4)
      .put(this.direction.x).put(this.direction.y).put(this.direction.z).put(0);
    return (T) this;
  }

  protected Vector3f direction() {
    return this.direction;
  }

  protected T color(Vector3f color) {
    this.color.set(color);
    this.buffer.asFloatBuffer().position(8)
      .put(this.color.x).put(this.color.y).put(this.color.z).put(0);
    return (T) this;
  }

  protected Vector3f color() {
    return this.color;
  }

  protected T intensity(float intensity) {
    this.intensity = intensity;
    this.buffer.asFloatBuffer().position(12).put(this.intensity);
    return (T) this;
  }

  protected float intensity() {
    return this.intensity;
  }

  protected T constant(float constant) {
    this.constant = constant;
    this.buffer.asFloatBuffer().position(13).put(this.constant);
    return (T) this;
  }

  protected float constant() {
    return this.constant;
  }

  protected T linear(float linear) {
    this.linear = linear;
    this.buffer.asFloatBuffer().position(14).put(this.linear);
    return (T) this;
  }

  protected float linear() {
    return this.linear;
  }

  protected T exponent(float exponent) {
    this.exponent = exponent;
    this.buffer.asFloatBuffer().position(15).put(this.exponent);
    return (T) this;
  }

  protected float exponent() {
    return this.exponent;
  }

  protected T cutOff(float cutOff) {
    this.cutOff = cutOff;
    this.buffer.asFloatBuffer().position(16).put(this.cutOff);
    return (T) this;
  }

  protected float cutOff() {
    return this.cutOff;
  }

  protected T cutOfAngle(float cutOfAngle) {
    this.cutOfAngle = cutOfAngle;
    this.buffer.asFloatBuffer().position(17).put(this.cutOfAngle);
    return (T) this;
  }

  protected float cutOfAngle() {
    return this.cutOfAngle;
  }

  public final LightType type() {
    return this.type;
  }

}
