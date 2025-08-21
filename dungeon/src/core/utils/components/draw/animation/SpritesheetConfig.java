package core.utils.components.draw.animation;

/**
 * Configuration for extracting frames from a spritesheet.
 *
 * <p>A {@link SpritesheetConfig} defines the size of each sprite, the grid layout (rows and
 * columns), and the starting offset ({@code x}, {@code y}) within the spritesheet image. This
 * information is used by {@link AnimationConfig} and related classes to cut a spritesheet into
 * individual frames.
 */
public class SpritesheetConfig {

  private int spriteWidth = 16;
  private int spriteHeight = 16;
  private int x = 0;
  private int y = 0;
  private int rows = 1;
  private int columns = 1;

  /** Creates a new {@link SpritesheetConfig} with default values (16×16 sprite, 1×1 grid). */
  public SpritesheetConfig() {}

  /**
   * Creates a new {@link SpritesheetConfig}.
   *
   * @param x x offset (in pixels) of the first sprite within the spritesheet
   * @param y y offset (in pixels) of the first sprite within the spritesheet
   * @param rows number of rows in the spritesheet grid
   * @param columns number of columns in the spritesheet grid
   * @param width width of a single sprite in pixels
   * @param height height of a single sprite in pixels
   */
  public SpritesheetConfig(int x, int y, int rows, int columns, int width, int height) {
    this.x = x;
    this.y = y;
    this.rows = rows;
    this.columns = columns;
    this.spriteWidth = width;
    this.spriteHeight = height;
  }

  /**
   * Creates a new {@link SpritesheetConfig} with custom grid size but default sprite size (16×16).
   *
   * @param x x offset in pixels
   * @param y y offset in pixels
   * @param rows number of rows in the spritesheet
   * @param columns number of columns in the spritesheet
   */
  public SpritesheetConfig(int x, int y, int rows, int columns) {
    this(x, y, rows, columns, 16, 16);
  }

  /**
   * Creates a new {@link SpritesheetConfig} with only an offset, using default sprite size (16×16)
   * and a 1×1 grid.
   *
   * @param x x offset in pixels
   * @param y y offset in pixels
   */
  public SpritesheetConfig(int x, int y) {
    this(x, y, 1, 1, 16, 16);
  }

  /**
   * @return the width of one sprite in pixels
   */
  public int spriteWidth() {
    return spriteWidth;
  }

  /**
   * Sets the width of a sprite in pixels.
   *
   * @param spriteWidth width of one sprite
   * @return this config for chaining
   */
  public SpritesheetConfig spriteWidth(int spriteWidth) {
    this.spriteWidth = spriteWidth;
    return this;
  }

  /**
   * @return the height of one sprite in pixels
   */
  public int spriteHeight() {
    return spriteHeight;
  }

  /**
   * Sets the height of a sprite in pixels.
   *
   * @param spriteHeight height of one sprite
   * @return this config for chaining
   */
  public SpritesheetConfig spriteHeight(int spriteHeight) {
    this.spriteHeight = spriteHeight;
    return this;
  }

  /**
   * @return the x offset in pixels
   */
  public int x() {
    return x;
  }

  /**
   * Sets the x offset in pixels.
   *
   * @param x horizontal offset
   * @return this config for chaining
   */
  public SpritesheetConfig x(int x) {
    this.x = x;
    return this;
  }

  /**
   * @return the y offset in pixels
   */
  public int y() {
    return y;
  }

  /**
   * Sets the y offset in pixels.
   *
   * @param y vertical offset
   * @return this config for chaining
   */
  public SpritesheetConfig y(int y) {
    this.y = y;
    return this;
  }

  /**
   * @return the number of rows in the spritesheet grid
   */
  public int rows() {
    return rows;
  }

  /**
   * Sets the number of rows in the spritesheet grid.
   *
   * @param rows number of rows
   * @return this config for chaining
   */
  public SpritesheetConfig rows(int rows) {
    this.rows = rows;
    return this;
  }

  /**
   * @return the number of columns in the spritesheet grid
   */
  public int columns() {
    return columns;
  }

  /**
   * Sets the number of columns in the spritesheet grid.
   *
   * @param columns number of columns
   * @return this config for chaining
   */
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
