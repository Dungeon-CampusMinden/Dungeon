package de.fwatermann.dungine.graphics.scene.light;

import org.joml.Vector3f;

/**
 * The `DirectionalLight` class represents a directional light source in a graphics scene.
 * It extends the `Light` class and provides methods to set and get the direction, color, and intensity of the light.
 */
public class DirectionalLight extends Light<DirectionalLight> {

  /**
   * Constructs a new `DirectionalLight` with the specified direction, color, and intensity.
   *
   * @param direction the direction of the light
   * @param color the color of the light
   * @param intensity the intensity of the light
   */
  public DirectionalLight(Vector3f direction, Vector3f color, float intensity) {
    super(LightType.DIRECTIONAL);
    this.direction.set(direction);
    this.color.set(color);
    this.intensity = intensity;
  }

  /**
   * Sets the direction of the light.
   *
   * @param direction the new direction of the light
   * @return this `DirectionalLight` instance
   */
  @Override
  public DirectionalLight direction(Vector3f direction) {
    return super.direction(direction);
  }

  /**
   * Returns the direction of the light.
   *
   * @return the direction of the light
   */
  @Override
  public Vector3f direction() {
    return super.direction();
  }

  /**
   * Sets the color of the light.
   *
   * @param color the new color of the light
   * @return this `DirectionalLight` instance
   */
  @Override
  public DirectionalLight color(Vector3f color) {
    return super.color(color);
  }

  /**
   * Returns the color of the light.
   *
   * @return the color of the light
   */
  @Override
  public Vector3f color() {
    return super.color();
  }

  /**
   * Sets the intensity of the light.
   *
   * @param intensity the new intensity of the light
   * @return this `DirectionalLight` instance
   */
  @Override
  public DirectionalLight intensity(float intensity) {
    return super.intensity(intensity);
  }

  /**
   * Returns the intensity of the light.
   *
   * @return the intensity of the light
   */
  @Override
  public float intensity() {
    return super.intensity();
  }
}
