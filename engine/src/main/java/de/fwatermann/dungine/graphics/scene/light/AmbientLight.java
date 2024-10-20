package de.fwatermann.dungine.graphics.scene.light;

import org.joml.Vector3f;

/**
 * The `AmbientLight` class represents an ambient light source in a 3D scene.
 * It extends the `Light` class and provides methods to set and get the color and intensity of the ambient light.
 */
public class AmbientLight extends Light<AmbientLight> {

  /**
   * Constructs an `AmbientLight` with the specified color and intensity.
   *
   * @param color the color of the ambient light.
   * @param intensity the intensity of the ambient light.
   */
  public AmbientLight(Vector3f color, float intensity) {
    super(LightType.AMBIENT);
    this.color.set(color);
    this.intensity = intensity;
  }

  /**
   * Sets the color of the ambient light.
   *
   * @param color the color to set.
   * @return the updated `AmbientLight` instance.
   */
  @Override
  public AmbientLight color(Vector3f color) {
    return super.color(color);
  }

  /**
   * Gets the color of the ambient light.
   *
   * @return the color of the ambient light.
   */
  @Override
  public Vector3f color() {
    return super.color();
  }

  /**
   * Sets the intensity of the ambient light.
   *
   * @param intensity the intensity to set.
   * @return the updated `AmbientLight` instance.
   */
  @Override
  public AmbientLight intensity(float intensity) {
    return super.intensity(intensity);
  }

  /**
   * Gets the intensity of the ambient light.
   *
   * @return the intensity of the ambient light.
   */
  @Override
  public float intensity() {
    return super.intensity();
  }
}
