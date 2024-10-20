package de.fwatermann.dungine.graphics.scene.light;

import org.joml.Vector3f;

/**
 * The `PointLight` class represents a point light source in a 3D scene.
 * It extends the `Light` class and provides methods to set and get the position, color, intensity, and attenuation factors of the light.
 */
public class PointLight extends Light<PointLight> {

  /**
   * Constructs a new `PointLight` instance with the specified parameters.
   *
   * @param position the position of the light
   * @param color the color of the light
   * @param intensity the intensity of the light
   * @param linear the linear attenuation factor
   * @param constant the constant attenuation factor
   * @param exponent the exponent attenuation factor
   */
  public PointLight(Vector3f position, Vector3f color, float intensity, float linear, float constant, float exponent) {
    super(LightType.POINT);
    this.position.set(position);
    this.color.set(color);
    this.intensity = intensity;
    this.linear = linear;
    this.constant = constant;
    this.exponent = exponent;
  }

  /**
   * Sets the position of the light.
   *
   * @param position the new position of the light
   * @return this `PointLight` instance for method chaining
   */
  @Override
  public PointLight position(Vector3f position) {
    return super.position(position);
  }

  /**
   * Gets the position of the light.
   *
   * @return the position of the light
   */
  @Override
  public Vector3f position() {
    return super.position();
  }

  /**
   * Sets the color of the light.
   *
   * @param color the new color of the light
   * @return this `PointLight` instance for method chaining
   */
  @Override
  protected PointLight color(Vector3f color) {
    return super.color(color);
  }

  /**
   * Gets the color of the light.
   *
   * @return the color of the light
   */
  @Override
  protected Vector3f color() {
    return super.color();
  }

  /**
   * Sets the intensity of the light.
   *
   * @param intensity the new intensity of the light
   * @return this `PointLight` instance for method chaining
   */
  @Override
  protected PointLight intensity(float intensity) {
    return super.intensity(intensity);
  }

  /**
   * Gets the intensity of the light.
   *
   * @return the intensity of the light
   */
  @Override
  protected float intensity() {
    return super.intensity();
  }

  /**
   * Sets the constant attenuation factor of the light.
   *
   * @param constant the new constant attenuation factor
   * @return this `PointLight` instance for method chaining
   */
  @Override
  protected PointLight constant(float constant) {
    return super.constant(constant);
  }

  /**
   * Gets the constant attenuation factor of the light.
   *
   * @return the constant attenuation factor of the light
   */
  @Override
  protected float constant() {
    return super.constant();
  }

  /**
   * Sets the linear attenuation factor of the light.
   *
   * @param linear the new linear attenuation factor
   * @return this `PointLight` instance for method chaining
   */
  @Override
  protected PointLight linear(float linear) {
    return super.linear(linear);
  }

  /**
   * Gets the linear attenuation factor of the light.
   *
   * @return the linear attenuation factor of the light
   */
  @Override
  protected float linear() {
    return super.linear();
  }

  /**
   * Sets the exponent attenuation factor of the light.
   *
   * @param exponent the new exponent attenuation factor
   * @return this `PointLight` instance for method chaining
   */
  @Override
  protected PointLight exponent(float exponent) {
    return super.exponent(exponent);
  }

  /**
   * Gets the exponent attenuation factor of the light.
   *
   * @return the exponent attenuation factor of the light
   */
  @Override
  protected float exponent() {
    return super.exponent();
  }
}
