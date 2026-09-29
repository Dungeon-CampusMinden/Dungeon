package rooms.programming.modules.loops;

import engine.utils.Point;
import java.util.ArrayList;
import java.util.List;

/** The same cell geometry drives world construction, execution and the terminal map. */
public final class LoopMaze {
  /** World tiles across one maze cell. */
  public static final int CELL_WIDTH = 5;

  /** World tiles along one maze cell. */
  public static final int CELL_HEIGHT = 3;

  /**
   * A position on the terminal grid.
   *
   * @param x column relative to the maze origin
   * @param y row relative to the maze origin
   */
  public record Cell(int x, int y) {
    /**
     * Finds the adjacent cell in a heading.
     *
     * @param direction heading to follow
     * @return the adjacent cell, which need not belong to the maze
     */
    public Cell next(Direction direction) {
      return new Cell(x + direction.dx(), y + direction.dy());
    }
  }

  /** Counterclockwise cardinal headings used by the program and map. */
  public enum Direction {
    EAST(1, 0),
    NORTH(0, 1),
    WEST(-1, 0),
    SOUTH(0, -1);
    private final int dx;
    private final int dy;

    Direction(int dx, int dy) {
      this.dx = dx;
      this.dy = dy;
    }

    /**
     * @return horizontal displacement of one step
     */
    public int dx() {
      return dx;
    }

    /**
     * @return vertical displacement of one step
     */
    public int dy() {
      return dy;
    }

    /**
     * @return heading after a quarter turn counterclockwise
     */
    public Direction left() {
      return values()[(ordinal() + 1) % 4];
    }

    /**
     * @return heading after a quarter turn clockwise
     */
    public Direction right() {
      return values()[(ordinal() + 3) % 4];
    }
  }

  /**
   * One ordered section of the maze.
   *
   * @param id stable challenge identifier
   * @param start initial cell
   * @param goal destination cell
   * @param facing starting heading
   */
  public record Checkpoint(String id, Cell start, Cell goal, Direction facing) {}

  private static final List<Checkpoint> CHECKPOINTS =
      List.of(
          new Checkpoint("forge-press", new Cell(0, 0), new Cell(3, 0), Direction.EAST),
          new Checkpoint("bellows", new Cell(3, 0), new Cell(3, 4), Direction.NORTH),
          new Checkpoint("chain-lift", new Cell(3, 4), new Cell(0, 4), Direction.WEST),
          new Checkpoint("cooling-channel", new Cell(0, 4), new Cell(0, 7), Direction.NORTH),
          new Checkpoint("heart-gate", new Cell(0, 7), new Cell(2, 9), Direction.EAST));
  private static final List<Cell> CELLS = buildCells();

  private LoopMaze() {}

  /**
   * @return all maze cells, including blind branches and hazards
   */
  public static List<Cell> cells() {
    return CELLS;
  }

  /**
   * @return checkpoints in progression order
   */
  public static List<Checkpoint> checkpoints() {
    return CHECKPOINTS;
  }

  /**
   * @return the guardian's cell
   */
  public static Cell monster() {
    return new Cell(2, 4);
  }

  /**
   * @return the pit cell
   */
  public static Cell pit() {
    return new Cell(0, 5);
  }

  /**
   * Converts a grid cell into the golem's physical anchor.
   *
   * @param origin world position of cell zero
   * @param cell cell to convert
   * @return the lower-left world anchor
   */
  public static Point world(Point origin, Cell cell) {
    return origin.translate(cell.x() * CELL_WIDTH, cell.y() * CELL_HEIGHT);
  }

  /**
   * Gives the goal arrow shown on the terminal.
   *
   * @param checkpoint zero-based checkpoint index
   * @return required heading at that checkpoint's goal
   */
  public static Direction goalFacing(int checkpoint) {
    return checkpoint + 1 < CHECKPOINTS.size()
        ? CHECKPOINTS.get(checkpoint + 1).facing()
        : Direction.EAST;
  }

  private static List<Cell> buildCells() {
    List<Cell> cells = new ArrayList<>();
    // The first goal is before the corridor end; the second is at the end of a longer run.
    for (int x = 0; x <= 4; x++) cells.add(new Cell(x, 0));
    for (int y = 1; y <= 4; y++) cells.add(new Cell(3, y));
    for (int x = 0; x < 3; x++) cells.add(new Cell(x, 4));
    for (int y = 5; y <= 7; y++) cells.add(new Cell(0, y));
    for (int x = 1; x <= 4; x++) cells.add(new Cell(x, 7));
    cells.addAll(
        List.of(
            new Cell(2, 1),
            new Cell(4, 3),
            new Cell(5, 3),
            new Cell(1, 3),
            new Cell(-1, 6),
            new Cell(2, 8),
            new Cell(1, 8),
            new Cell(2, 9)));
    return List.copyOf(cells);
  }
}
