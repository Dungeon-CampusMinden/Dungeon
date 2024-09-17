package de.fwatermann.dungine.graphics.scene.light;

import org.joml.Vector3f;

public class PointLight extends Light<PointLight> {

  public PointLight(Vector3f position, Vector3f color, float intensity, float linear, float constant, float exponent) {
    super(LightType.POINT);
    this.position.set(position);
    this.color.set(color);
    this.intensity = intensity;
    this.linear = linear;
    this.constant = constant;
    this.exponent = exponent;
  }

  @Override
  public PointLight position(Vector3f position) {
    return super.position(position);
  }

  @Override
  public Vector3f position() {
    return super.position();
  }

  @Override
  protected PointLight color(Vector3f color) {
    return super.color(color);
  }

  @Override
  protected Vector3f color() {
    return super.color();
  }

  @Override
  protected PointLight intensity(float intensity) {
    return super.intensity(intensity);
  }

  @Override
  protected float intensity() {
    return super.intensity();
  }

  @Override
  protected PointLight constant(float constant) {
    return super.constant(constant);
  }

  @Override
  protected float constant() {
    return super.constant();
  }

  @Override
  protected PointLight linear(float linear) {
    return super.linear(linear);
  }

  @Override
  protected float linear() {
    return super.linear();
  }

  @Override
  protected PointLight exponent(float exponent) {
    return super.exponent(exponent);
  }

  @Override
  protected float exponent() {
    return super.exponent();
  }
}
