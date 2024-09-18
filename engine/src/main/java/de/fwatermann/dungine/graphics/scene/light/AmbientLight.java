package de.fwatermann.dungine.graphics.scene.light;

import org.joml.Vector3f;

public class AmbientLight extends Light<AmbientLight> {

  public AmbientLight(Vector3f color, float intensity) {
    super(LightType.AMBIENT);
    this.color.set(color);
    this.intensity = intensity;
  }

  @Override
  public AmbientLight color(Vector3f color) {
    return super.color(color);
  }

  @Override
  public Vector3f color() {
    return super.color();
  }

  @Override
  public AmbientLight intensity(float intensity) {
    return super.intensity(intensity);
  }

  @Override
  public float intensity() {
    return super.intensity();
  }
}
