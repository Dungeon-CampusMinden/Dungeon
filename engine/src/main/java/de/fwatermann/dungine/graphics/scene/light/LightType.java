package de.fwatermann.dungine.graphics.scene.light;

public enum LightType {

  POINT(0),
  SPOTLIGHT(1),
  DIRECTIONAL(2),
  AMBIENT(3);

  private int id = 0;

  LightType(int type) {
    this.id = type;
  }

  public int id() {
    return this.id;
  }

}
