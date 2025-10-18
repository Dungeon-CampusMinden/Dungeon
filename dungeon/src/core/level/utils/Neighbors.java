package core.level.utils;

public final class Neighbors {
  private final Coordinate p;
  private final Coordinate left;
  private final Coordinate right;
  private final Coordinate up;
  private final Coordinate down;
  private final Coordinate upLeft;
  private final Coordinate upRight;
  private final Coordinate downLeft;
  private final Coordinate downRight;

  private final LevelElement leftE;
  private final LevelElement rightE;
  private final LevelElement upE;
  private final LevelElement downE;
  private final LevelElement upLeftE;
  private final LevelElement upRightE;
  private final LevelElement downLeftE;
  private final LevelElement downRightE;
  private final LevelElement centerE;

  public Neighbors(Coordinate center, LevelElement[][] layout) {
    this.p = center;

    this.left = offset(center, -1, 0);
    this.right = offset(center, 1, 0);
    this.up = offset(center, 0, 1);
    this.down = offset(center, 0, -1);
    this.upLeft = offset(center, -1, 1);
    this.upRight = offset(center, 1, 1);
    this.downLeft = offset(center, -1, -1);
    this.downRight = offset(center, 1, -1);

    this.centerE = elem(layout, this.p);
    this.leftE = elem(layout, this.left);
    this.rightE = elem(layout, this.right);
    this.upE = elem(layout, this.up);
    this.downE = elem(layout, this.down);
    this.upLeftE = elem(layout, this.upLeft);
    this.upRightE = elem(layout, this.upRight);
    this.downLeftE = elem(layout, this.downLeft);
    this.downRightE = elem(layout, this.downRight);
  }

  public static Neighbors of(Coordinate center, LevelElement[][] layout) {
    return new Neighbors(center, layout);
  }

  private static Coordinate offset(Coordinate base, int dx, int dy) {
    return new Coordinate(base.x() + dx, base.y() + dy);
  }

  private static LevelElement elem(LevelElement[][] layout, Coordinate c) {
    return TileTextureFactory.get(layout, c.x(), c.y());
  }

  public Coordinate getP() {
    return p;
  }

  public Coordinate getLeft() {
    return left;
  }

  public Coordinate getRight() {
    return right;
  }

  public Coordinate getUp() {
    return up;
  }

  public Coordinate getDown() {
    return down;
  }

  public Coordinate getUpLeft() {
    return upLeft;
  }

  public Coordinate getUpRight() {
    return upRight;
  }

  public Coordinate getDownLeft() {
    return downLeft;
  }

  public Coordinate getDownRight() {
    return downRight;
  }

  public LevelElement getLeftE() {
    return leftE;
  }

  public LevelElement getRightE() {
    return rightE;
  }

  public LevelElement getUpE() {
    return upE;
  }

  public LevelElement getDownE() {
    return downE;
  }

  public LevelElement getUpLeftE() {
    return upLeftE;
  }

  public LevelElement getUpRightE() {
    return upRightE;
  }

  public LevelElement getDownLeftE() {
    return downLeftE;
  }

  public LevelElement getDownRightE() {
    return downRightE;
  }

  public LevelElement getCenterE() {
    return centerE;
  }
}
