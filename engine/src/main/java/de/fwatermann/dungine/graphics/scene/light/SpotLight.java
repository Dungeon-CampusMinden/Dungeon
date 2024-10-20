package de.fwatermann.dungine.graphics.scene.light;

import org.joml.Vector3f;

/**
 * Represents a spotlight in the scene with various properties such as position, direction, color, intensity, and attenuation factors.
 */
public class SpotLight extends Light<SpotLight> {

  /**
   * Constructs a new SpotLight with the specified parameters.
   *
   * @param position the position of the spotlight
   * @param direction the direction the spotlight is pointing
   * @param color the color of the light
   * @param intensity the intensity of the light
   * @param constant the constant attenuation factor
   * @param linear the linear attenuation factor
   * @param exponent the exponential attenuation factor
   * @param cutOff the cutoff value for the spotlight
   * @param cutOffAngle the cutoff angle for the spotlight
   */
  public SpotLight(Vector3f position, Vector3f direction, Vector3f color, float intensity, float constant, float linear, float exponent, float cutOff, float cutOffAngle)  {
    super(LightType.SPOTLIGHT);
    this.position.set(position);
    this.direction.set(direction);
    this.color.set(color);
    this.intensity = intensity;
    this.constant = constant;
    this.linear = linear;
    this.exponent = exponent;
    this.cutOff = cutOff;
    this.cutOfAngle = cutOffAngle;
  }

  /**
   * Gets the cutoff angle of the spotlight.
   *
   * @return the cutoff angle
   */
  @Override
  public float cutOfAngle() {
    return super.cutOfAngle();
  }

  /**
   * Sets the cutoff angle of the spotlight.
   *
   * @param cutOfAngle the new cutoff angle
   * @return the updated SpotLight instance
   */
  @Override
  public SpotLight cutOfAngle(float cutOfAngle) {
    return super.cutOfAngle(cutOfAngle);
  }

  /**
   * Gets the cutoff value of the spotlight.
   *
   * @return the cutoff value
   */
  @Override
  public float cutOff() {
    return super.cutOff();
  }

  /**
   * Sets the cutoff value of the spotlight.
   *
   * @param cutOff the new cutoff value
   * @return the updated SpotLight instance
   */
  @Override
  public SpotLight cutOff(float cutOff) {
    return super.cutOff(cutOff);
  }

  /**
   * Gets the exponential attenuation factor of the spotlight.
   *
   * @return the exponential attenuation factor
   */
  @Override
  public float exponent() {
    return super.exponent();
  }

  /**
   * Sets the exponential attenuation factor of the spotlight.
   *
   * @param exponent the new exponential attenuation factor
   * @return the updated SpotLight instance
   */
  @Override
  public SpotLight exponent(float exponent) {
    return super.exponent(exponent);
  }

  /**
   * Gets the linear attenuation factor of the spotlight.
   *
   * @return the linear attenuation factor
   */
  @Override
  public float linear() {
    return super.linear();
  }

  /**
   * Sets the linear attenuation factor of the spotlight.
   *
   * @param linear the new linear attenuation factor
   * @return the updated SpotLight instance
   */
  @Override
  public SpotLight linear(float linear) {
    return super.linear(linear);
  }

  /**
   * Gets the constant attenuation factor of the spotlight.
   *
   * @return the constant attenuation factor
   */
  @Override
  public float constant() {
    return super.constant();
  }

  /**
   * Sets the constant attenuation factor of the spotlight.
   *
   * @param constant the new constant attenuation factor
   * @return the updated SpotLight instance
   */
  @Override
  public SpotLight constant(float constant) {
    return super.constant(constant);
  }

  /**
   * Gets the intensity of the spotlight.
   *
   * @return the intensity
   */
  @Override
  public float intensity() {
    return super.intensity();
  }

  /**
   * Sets the intensity of the spotlight.
   *
   * @param intensity the new intensity
   * @return the updated SpotLight instance
   */
  @Override
  public SpotLight intensity(float intensity) {
    return super.intensity(intensity);
  }

  /**
   * Gets the color of the spotlight.
   *
   * @return the color
   */
  @Override
  public Vector3f color() {
    return super.color();
  }

  /**
   * Sets the color of the spotlight.
   *
   * @param color the new color
   * @return the updated SpotLight instance
   */
  @Override
  public SpotLight color(Vector3f color) {
    return super.color(color);
  }

  /**
   * Gets the direction of the spotlight.
   *
   * @return the direction
   */
  @Override
  public Vector3f direction() {
    return super.direction();
  }

  /**
   * Sets the direction of the spotlight.
   *
   * @param direction the new direction
   * @return the updated SpotLight instance
   */
  @Override
  public SpotLight direction(Vector3f direction) {
    return super.direction(direction);
  }

  /**
   * Gets the position of the spotlight.
   *
   * @return the position
   */
  @Override
  public Vector3f position() {
    return super.position();
  }

  /**
   * Sets the position of the spotlight.
   *
   * @param position the new position
   * @return the updated SpotLight instance
   */
  @Override
  public SpotLight position(Vector3f position) {
    return super.position(position);
  }
}
