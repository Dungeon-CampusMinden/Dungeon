package core.utils.components.draw.animation;

public class SpritesheetConfig {

  private int spriteWidth = 16;
  private int spriteHeight = 16;
  private int x = 0;
  private int y = 0;
  private int rows = 1;
  private int columns = 1;

  public SpritesheetConfig() {}

  public SpritesheetConfig(int x, int y, int rows, int columns, int width, int height) {
    this.x = x;
    this.y = y;
    this.rows = rows;
    this.columns = columns;
    this.spriteWidth = width;
    this.spriteHeight = height;
  }

  public SpritesheetConfig(int x, int y, int rows, int columns) {
    this(x, y, rows, columns, 16, 16);
  }

  public SpritesheetConfig(int x, int y) {
    this(x, y, 1, 1, 16, 16);
  }

  public int spriteWidth() {
    return spriteWidth;
  }

  public SpritesheetConfig spriteWidth(int spriteWidth) {
    this.spriteWidth = spriteWidth;
    return this;
  }

  public int spriteHeight() {
    return spriteHeight;
  }

  public SpritesheetConfig spriteHeight(int spriteHeight) {
    this.spriteHeight = spriteHeight;
    return this;
  }

  public int x() {
    return x;
  }

  public SpritesheetConfig x(int x) {
    this.x = x;
    return this;
  }

  public int y() {
    return y;
  }

  public SpritesheetConfig y(int y) {
    this.y = y;
    return this;
  }

  public int rows() {
    return rows;
  }

  public SpritesheetConfig rows(int rows) {
    this.rows = rows;
    return this;
  }

  public int columns() {
    return columns;
  }

  public SpritesheetConfig columns(int columns) {
    this.columns = columns;
    return this;
  }

  @Override
  public String toString() {
    return "SpritesheetConfig{"
        + "spriteWidth="
        + spriteWidth
        + ", spriteHeight="
        + spriteHeight
        + ", x="
        + x
        + ", y="
        + y
        + ", rows="
        + rows
        + ", columns="
        + columns
        + '}';
  }
}
