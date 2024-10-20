package de.fwatermann.dungine.graphics.scene.light;

/**
 * The `LightType` enum represents different types of lights that can be used in the graphics
 * engine. Each light type is associated with a unique identifier.
 */
public enum LightType {

  /** Point light type. */
  POINT(0),

  /** Spotlight type. */
  SPOTLIGHT(1),

  /** Directional light type. */
  DIRECTIONAL(2),

  /** Ambient light type. */
  AMBIENT(3);

  private int id = 0;

  /**
   * Constructs a new `LightType` with the specified type identifier.
   *
   * @param type the identifier for the light type
   */
  LightType(int type) {
    this.id = type;
  }

  /**
   * Gets the identifier of the light type.
   *
   * @return the identifier of the light type
   */
  public int id() {
    return this.id;
  }
}
