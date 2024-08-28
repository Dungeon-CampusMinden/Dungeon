package de.fwatermann.dungine.graphics;

/** Enum representing the different billboard modes for the sprite. */
public enum BillboardMode {

  /** No billboarding. The sprite is rendered as a plane in the world. */
  NONE(0),

  /** Sphere billboarding. The sprite is always facing the camera. */
  SPHERICAL(1),

  /**
   * Cylinder billboarding. The sprite is always facing the camera, but only rotates around the
   * y-axis.
   */
  CYLINDRICAL(2);
  public final int value;

  BillboardMode(int value) {
    this.value = value;
  }
}
