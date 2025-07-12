package core.level.generator.randomwalk;

import core.level.TileLevel;
import core.level.elements.ILevel;
import core.level.generator.IGenerator;
import core.level.utils.Coordinate;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.level.utils.LevelSize;
import core.utils.Vector2;
import java.util.Random;

/** Generates levels using random walk algorithm. */
public class RandomWalkGenerator implements IGenerator {
  private static final Random RANDOM = new Random();
  private static final int SMALL_MIN_X_SIZE = 10;
  private static final int SMALL_MIN_Y_SIZE = 10;
  private static final int SMALL_MAX_X_SIZE = 30;
  private static final int SMALL_MAX_Y_SIZE = 30;
  private static final int MEDIUM_MIN_X_SIZE = 30;
  private static final int MEDIUM_MIN_Y_SIZE = 30;
  private static final int MEDIUM_MAX_X_SIZE = 100;
  private static final int MEDIUM_MAX_Y_SIZE = 100;
  private static final int BIG_MIN_X_SIZE = 100;
  private static final int BIG_MIN_Y_SIZE = 100;
  private static final int BIG_MAX_X_SIZE = 300;
  private static final int BIG_MAX_Y_SIZE = 300;
  private static final int MIN_STEPS_FACTOR = 4;
  private static final int MAX_STEPS_FACTOR = 2;

  @Override
  public ILevel level(DesignLabel designLabel, LevelSize size) {
    return new TileLevel(layout(size), designLabel);
  }

  /**
   * Generates the floor layout to a specified level size.
   *
   * @param size size of the level to be generated
   * @return layout of the level
   */
  public LevelElement[][] layout(LevelSize size) {
    return switch (size) {
      case SMALL ->
          drunkWalk(
              new MinMaxValue(SMALL_MIN_X_SIZE, SMALL_MAX_X_SIZE),
              new MinMaxValue(SMALL_MIN_Y_SIZE, SMALL_MAX_Y_SIZE));
      case LARGE ->
          drunkWalk(
              new MinMaxValue(BIG_MIN_X_SIZE, BIG_MAX_X_SIZE),
              new MinMaxValue(BIG_MIN_Y_SIZE, BIG_MAX_Y_SIZE));
      default ->
          drunkWalk(
              new MinMaxValue(MEDIUM_MIN_X_SIZE, MEDIUM_MAX_X_SIZE),
              new MinMaxValue(MEDIUM_MIN_Y_SIZE, MEDIUM_MAX_Y_SIZE));
    };
  }

  private LevelElement[][] drunkWalk(MinMaxValue minMaxValueX, MinMaxValue minMaxValueY) {
    int xSize = RANDOM.nextInt(minMaxValueX.min(), minMaxValueX.max());
    int ySize = RANDOM.nextInt(minMaxValueY.min(), minMaxValueY.max());
    LevelElement[][] layout = new LevelElement[ySize][xSize];
    for (int y = 0; y < ySize; y++) {
      for (int x = 0; x < xSize; x++) {
        layout[y][x] = LevelElement.SKIP;
      }
    }

    Coordinate position = new Coordinate(RANDOM.nextInt(0, xSize), RANDOM.nextInt(0, ySize));
    int steps =
        RANDOM.nextInt((xSize * ySize) / MIN_STEPS_FACTOR, (xSize * ySize) / MAX_STEPS_FACTOR);
    for (; steps > 0; steps--) {
      layout[position.y()][position.x()] = LevelElement.FLOOR;

      Vector2 dir;
      if (RANDOM.nextBoolean()) {
        dir = RANDOM.nextBoolean() ? Vector2.RIGHT : Vector2.LEFT;
      } else {
        dir = RANDOM.nextBoolean() ? Vector2.UP : Vector2.DOWN;
      }

      Coordinate moved = position.translate(dir);
      int clampedX = Math.min(Math.max(moved.x(), 0), xSize - 1);
      int clampedY = Math.min(Math.max(moved.y(), 0), ySize - 1);
      position = new Coordinate(clampedX, clampedY);
    }
    return layout;
  }

  private record MinMaxValue(int min, int max) {}
}
