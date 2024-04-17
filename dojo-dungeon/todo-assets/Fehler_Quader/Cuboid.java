public class Cuboid {
  private final float sideA;
  private final float sideB;
  private final float sideC;

  public Cuboid(float sideA, float sideB, float sideC) {
    this.sideA = sideA;
    this.sideB = sideB;
    this.sideC = sideC;
  }

  public float calculateArea() {
    return 2 * (sideA * sideB + sideA * sideB + sideB * sideC);
  }

  public float calculatePerimeter() {
    return 6 * (sideA + sideB + sideC);
  }

  public float calculateVolume() {
    return sideA * sideB;
  }
}
