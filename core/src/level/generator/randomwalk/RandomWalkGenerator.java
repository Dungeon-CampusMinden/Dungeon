package level.generator.randomwalk;

import java.util.Random;
import level.elements.ILevel;
import level.elements.TileLevel;
import level.generator.IGenerator;
import level.tools.Coordinate;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import level.tools.LevelSize;

public class RandomWalkGenerator implements IGenerator {
    private record MinMaxValue(int min, int max) {}

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
    public ILevel getLevel(DesignLabel designLabel, LevelSize size) {
        return new TileLevel(getLayout(size), designLabel);
    }

    /**
     * Generates the floor layout to a specified level size
     *
     * @param size size of the level to be generated
     * @return layout of the level
     */
    public LevelElement[][] getLayout(LevelSize size) {
        return switch (size) {
            case SMALL -> drunkWalk(
                    new MinMaxValue(SMALL_MIN_X_SIZE, SMALL_MAX_X_SIZE),
                    new MinMaxValue(SMALL_MIN_Y_SIZE, SMALL_MAX_Y_SIZE));
            case LARGE -> drunkWalk(
                    new MinMaxValue(BIG_MIN_X_SIZE, BIG_MAX_X_SIZE),
                    new MinMaxValue(BIG_MIN_Y_SIZE, BIG_MAX_Y_SIZE));
            default -> drunkWalk(
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
                RANDOM.nextInt(
                        (xSize * ySize) / MIN_STEPS_FACTOR, (xSize * ySize) / MAX_STEPS_FACTOR);
        for (; steps > 0; steps--) {
            layout[position.y][position.x] = LevelElement.FLOOR;

            if (RANDOM.nextBoolean()) {
                if (RANDOM.nextBoolean()) {
                    position.x = Math.min(position.x + 1, xSize - 1);
                } else {
                    position.x = Math.max(position.x - 1, 0);
                }
            } else {
                if (RANDOM.nextBoolean()) {
                    position.y = Math.min(position.y + 1, ySize - 1);
                } else {
                    position.y = Math.max(position.y - 1, 0);
                }
            }
        }

        // pick random floor tile as exit
        Coordinate c = getRandomFloor(layout);
        layout[c.y][c.x] = LevelElement.EXIT;

        return layout;
    }

    private Coordinate getRandomFloor(LevelElement[][] layout) {
        Coordinate coordinate =
                new Coordinate(RANDOM.nextInt(layout[0].length), RANDOM.nextInt(layout.length));
        LevelElement randomTile = layout[coordinate.y][coordinate.x];
        if (randomTile == LevelElement.FLOOR) {
            return coordinate;
        } else {
            return getRandomFloor(layout);
        }
    }
}
