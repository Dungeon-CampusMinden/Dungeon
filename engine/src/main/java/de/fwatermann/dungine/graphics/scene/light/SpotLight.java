package de.fwatermann.dungine.graphics.scene.light;

import org.joml.Vector3f;

public class SpotLight extends Light<SpotLight> {

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

  @Override
  public float cutOfAngle() {
    return super.cutOfAngle();
  }

  @Override
  public SpotLight cutOfAngle(float cutOfAngle) {
    return super.cutOfAngle(cutOfAngle);
  }

  @Override
  public float cutOff() {
    return super.cutOff();
  }

  @Override
  public SpotLight cutOff(float cutOff) {
    return super.cutOff(cutOff);
  }

  @Override
  public float exponent() {
    return super.exponent();
  }

  @Override
  public SpotLight exponent(float exponent) {
    return super.exponent(exponent);
  }

  @Override
  public float linear() {
    return super.linear();
  }

  @Override
  public SpotLight linear(float linear) {
    return super.linear(linear);
  }

  @Override
  public float constant() {
    return super.constant();
  }

  @Override
  public SpotLight constant(float constant) {
    return super.constant(constant);
  }

  @Override
  public float intensity() {
    return super.intensity();
  }

  @Override
  public SpotLight intensity(float intensity) {
    return super.intensity(intensity);
  }

  @Override
  public Vector3f color() {
    return super.color();
  }

  @Override
  public SpotLight color(Vector3f color) {
    return super.color(color);
  }

  @Override
  public Vector3f direction() {
    return super.direction();
  }

  @Override
  public SpotLight direction(Vector3f direction) {
    return super.direction(direction);
  }

  @Override
  public Vector3f position() {
    return super.position();
  }

  @Override
  public SpotLight position(Vector3f position) {
    return super.position(position);
  }
}
