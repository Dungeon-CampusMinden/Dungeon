package de.fwatermann.dungine.graphics.camera;

public class CameraViewport {

  private float width, height, offsetX, offsetY;

  public CameraViewport(float width, float height, float offsetX, float offsetY) {
    this.width = width;
    this.height = height;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
  }

  public void set(float width, float height, float offsetX, float offsetY) {
    this.width = width;
    this.height = height;
    this.offsetX = offsetX;
    this.offsetY = offsetY;
  }

  public float width() {
    return this.width;
  }

  public CameraViewport width(float width) {
    this.width = width;
    return this;
  }

  public float height() {
    return this.height;
  }

  public CameraViewport height(float height) {
    this.height = height;
    return this;
  }

  public float offsetX() {
    return this.offsetX;
  }

  public CameraViewport offsetX(float offsetX) {
    this.offsetX = offsetX;
    return this;
  }

  public float offsetY() {
    return this.offsetY;
  }

  public CameraViewport offsetY(float offsetY) {
    this.offsetY = offsetY;
    return this;
  }
}
