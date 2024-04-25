/** This class represents a Cuboid (3D box). */
public class Cuboid {
  private final float sideA;
  private final float sideB;
  private final float sideC;

  /**
   * Constructs the Cuboid.
   *
   * @param sideA side length a of the Cuboid
   * @param sideB side length b of the Cuboid
   * @param sideC side length c of the Cuboid
   */
  public Cuboid(float sideA, float sideB, float sideC) {
    this.sideA = sideA;
    this.sideB = sideB;
    this.sideC = sideC;
  }

  /**
   * Calculate the area of the Cuboid.
   *
   * <p>Area refers to the measurement of the surface enclosed by a shape.
   *
   * @return the area
   */
  public float calculateArea() {
    return 2 * (sideA * sideB + sideA * sideB + sideB * sideC);
  }

  /**
   * Calculate the perimeter of the Cuboid.
   *
   * <p>Perimeter refers to the measurement of the boundary of a shape.
   *
   * @return the perimeter
   */
  public float calculatePerimeter() {
    return 6 * (sideA + sideB + sideC);
  }

  /**
   * Calculate the volume of the Cuboid.
   *
   * <p>Volume refers to the measurement of the space enclosed by a three-dimensional object.
   *
   * @return the volume
   */
  public float calculateVolume() {
    return sideA * sideB;
  }
}
