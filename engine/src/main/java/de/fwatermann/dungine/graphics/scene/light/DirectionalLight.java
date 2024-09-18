package de.fwatermann.dungine.graphics.scene.light;

import org.joml.Vector3f;

public class DirectionalLight extends Light<DirectionalLight> {

  public DirectionalLight(Vector3f direction, Vector3f color, float intensity) {
    super(LightType.DIRECTIONAL);
    this.direction.set(direction);
    this.color.set(color);
    this.intensity = intensity;
  }

  @Override
  public DirectionalLight direction(Vector3f direction) {
    return super.direction(direction);
  }

  @Override
  public Vector3f direction() {
    return super.direction();
  }

  @Override
  public DirectionalLight color(Vector3f color) {
    return super.color(color);
  }

  @Override
  public Vector3f color() {
    return super.color();
  }

  @Override
  public DirectionalLight intensity(float intensity) {
    return super.intensity(intensity);
  }

  @Override
  public float intensity() {
    return super.intensity();
  }
}
