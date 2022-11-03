package generator.room;

import static level.elements.ILevel.RANDOM;

import level.elements.ILevel;
import level.elements.TileLevel;
import level.generator.IGenerator;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import level.tools.LevelSize;

import java.util.Random;

public class RoomGenerator implements IGenerator {

    private record MinMaxValue(int min, int max) {}

    private static final int SMALL_MIN_X_SIZE = 5;
    private static final int SMALL_MIN_Y_SIZE = 5;
    private static final int SMALL_MAX_X_SIZE = 8;
    private static final int SMALL_MAX_Y_SIZE = 8;
    private static final int MEDIUM_MIN_X_SIZE = 12;
    private static final int MEDIUM_MIN_Y_SIZE = 12;
    private static final int MEDIUM_MAX_X_SIZE = 16;
    private static final int MEDIUM_MAX_Y_SIZE = 16;
    private static final int BIG_MIN_X_SIZE = 20;
    private static final int BIG_MIN_Y_SIZE = 20;
    private static final int BIG_MAX_X_SIZE = 24;
    private static final int BIG_MAX_Y_SIZE = 24;

    @Override
    public ILevel getLevel(DesignLabel designLabel, LevelSize size) {
        return new TileLevel(getLayout(size), designLabel);
    }

    public LevelElement[][] getLayout(LevelSize size) {
        return generateRoom(size, RANDOM.nextLong());
    }

    private LevelElement[][] generateRoom(LevelSize size, long seed) {
        Random random = new Random(seed);
        MinMaxValue outerRoomBoundsX;
        MinMaxValue outerRoomBoundsY;

        switch (size) {
            case SMALL -> {
                outerRoomBoundsX = new MinMaxValue(SMALL_MIN_X_SIZE, SMALL_MAX_X_SIZE);
                outerRoomBoundsY = new MinMaxValue(SMALL_MIN_Y_SIZE, SMALL_MAX_Y_SIZE);
            }
            case LARGE -> {
                outerRoomBoundsX = new MinMaxValue(BIG_MIN_X_SIZE, BIG_MAX_X_SIZE);
                outerRoomBoundsY = new MinMaxValue(BIG_MIN_Y_SIZE, BIG_MAX_Y_SIZE);
            }
            default -> {
                outerRoomBoundsX = new MinMaxValue(MEDIUM_MIN_X_SIZE, MEDIUM_MAX_X_SIZE);
                outerRoomBoundsY = new MinMaxValue(MEDIUM_MIN_Y_SIZE, MEDIUM_MAX_Y_SIZE);
            }
        }

        int xSize = random.nextInt(outerRoomBoundsX.min(), outerRoomBoundsX.max() + 1);
        int ySize = random.nextInt(outerRoomBoundsY.min(), outerRoomBoundsY.max() + 1);

        // Initialize layout with additional buffer for wall and skip layer
        final int wallBuffer = 2;
        LevelElement[][] layout = new LevelElement[ySize + wallBuffer * 2][xSize + wallBuffer * 2];

        // Fill with skip
        for (int y = 0; y < layout.length; y++) {
            for (int x = 0; x < layout[0].length; x++) {
                layout[y][x] = LevelElement.SKIP;
            }
        }

        // Add BaseFloor in the center
        final MinMaxValue BASE_FLOOR_X_SIZE;
        final MinMaxValue BASE_FLOOR_Y_SIZE;
        switch (size) {
            case SMALL -> {
                BASE_FLOOR_X_SIZE = new MinMaxValue(SMALL_MIN_X_SIZE, xSize);
                BASE_FLOOR_Y_SIZE = new MinMaxValue(SMALL_MIN_Y_SIZE, ySize);
            }
            case LARGE -> {
                BASE_FLOOR_X_SIZE = new MinMaxValue(BIG_MIN_X_SIZE - 6, xSize);
                BASE_FLOOR_Y_SIZE = new MinMaxValue(BIG_MIN_Y_SIZE - 6, ySize);
            }
            default -> {
                BASE_FLOOR_X_SIZE = new MinMaxValue(MEDIUM_MIN_X_SIZE - 4, xSize);
                BASE_FLOOR_Y_SIZE = new MinMaxValue(MEDIUM_MIN_Y_SIZE - 4, ySize);
            }
        }
        final MinMaxValue BASE_FLOOR_PADDING_X =
                new MinMaxValue(
                        (xSize - BASE_FLOOR_X_SIZE.max + 1) / 2,
                        (xSize - BASE_FLOOR_X_SIZE.min) / 2);
        final MinMaxValue BASE_FLOOR_PADDING_Y =
                new MinMaxValue(
                        (ySize - BASE_FLOOR_Y_SIZE.max + 1) / 2,
                        (ySize - BASE_FLOOR_Y_SIZE.min) / 2);

        int baseFloorPaddingX =
                BASE_FLOOR_PADDING_X.min == BASE_FLOOR_PADDING_X.max
                        ? BASE_FLOOR_PADDING_X.min
                        : random.nextInt(BASE_FLOOR_PADDING_X.min, BASE_FLOOR_PADDING_X.max + 1);
        int baseFloorPaddingY =
                BASE_FLOOR_PADDING_Y.min == BASE_FLOOR_PADDING_Y.max
                        ? BASE_FLOOR_PADDING_Y.min
                        : random.nextInt(BASE_FLOOR_PADDING_Y.min, BASE_FLOOR_PADDING_Y.max + 1);
        int baseFloorY = ySize - 2 * baseFloorPaddingY;
        int baseFloorX = xSize - 2 * baseFloorPaddingX;

        for (int y = wallBuffer + baseFloorPaddingY;
                y < layout.length - wallBuffer - baseFloorPaddingY;
                y++) {
            for (int x = wallBuffer + baseFloorPaddingX;
                    x < layout[0].length - wallBuffer - baseFloorPaddingX;
                    x++) {
                layout[y][x] = LevelElement.FLOOR;
            }
        }

        // extend base floor
        boolean symmetrical = false;
        boolean up = false;
        boolean down = false;
        boolean left = false;
        boolean right = false;
        boolean upperLeft = false;
        boolean upperRight = false;
        boolean lowerLeft = false;
        boolean lowerRight = false;
        if (random.nextFloat() > 0.5) {
            symmetrical = true;
        }
        // Small rooms cannot extend to the corners
        // Medium and Big Rooms can extend to corner or sides
        if (size == LevelSize.SMALL || random.nextFloat() > 0.5) {
            // extend to sides
            if (random.nextFloat() < 0.75) {
                up = true;
            }
            if (random.nextFloat() < 0.75) {
                down = true;
            }
            if (random.nextFloat() < 0.75) {
                left = true;
            }
            if (random.nextFloat() < 0.75) {
                right = true;
            }
        } else {
            // extend to corners
            if (random.nextFloat() < 0.75) {
                upperLeft = true;
            }
            if (random.nextFloat() < 0.75) {
                upperRight = true;
            }
            if (random.nextFloat() < 0.75) {
                lowerLeft = true;
            }
            if (random.nextFloat() < 0.75) {
                lowerRight = true;
            }
        }
        if (up) {
            System.out.println("UP");
            for (int y = wallBuffer + baseFloorPaddingY + baseFloorY; y < wallBuffer + ySize; y++) {
                for (int x = wallBuffer + baseFloorPaddingX;
                        x < wallBuffer + baseFloorPaddingX + baseFloorX;
                        x++) {
                    layout[y][x] = LevelElement.FLOOR;
                }
            }
        }
        if (down) {
            System.out.println("DOWN");
            for (int y = wallBuffer; y < wallBuffer + baseFloorPaddingY; y++) {
                for (int x = wallBuffer + baseFloorPaddingX;
                        x < wallBuffer + baseFloorPaddingX + baseFloorX;
                        x++) {
                    layout[y][x] = LevelElement.FLOOR;
                }
            }
        }
        if (left) {
            System.out.println("LEFT");
            for (int y = wallBuffer + baseFloorPaddingY;
                    y < wallBuffer + baseFloorPaddingY + baseFloorY;
                    y++) {
                for (int x = wallBuffer; x < wallBuffer + baseFloorPaddingX; x++) {
                    layout[y][x] = LevelElement.FLOOR;
                }
            }
        }
        if (right) {
            System.out.println("RIGHT");
            for (int y = wallBuffer + baseFloorPaddingY;
                    y < wallBuffer + baseFloorPaddingY + baseFloorY;
                    y++) {
                for (int x = wallBuffer + baseFloorPaddingX + baseFloorX;
                        x < wallBuffer + xSize;
                        x++) {
                    layout[y][x] = LevelElement.FLOOR;
                }
            }
        }
        if (upperLeft) {
            System.out.println("UPPER LEFT");
            for (int y = wallBuffer + baseFloorPaddingY + baseFloorY - baseFloorY / 2 + 1;
                    y < wallBuffer + ySize;
                    y++) {
                for (int x = wallBuffer;
                        x < wallBuffer + baseFloorPaddingX + baseFloorX / 2 - 1;
                        x++) {
                    layout[y][x] = LevelElement.FLOOR;
                }
            }
        }
        if (upperRight) {
            System.out.println("UPPER RIGHT");
            for (int y = wallBuffer + baseFloorPaddingY + baseFloorY - baseFloorY / 2 + 1;
                    y < wallBuffer + ySize;
                    y++) {
                for (int x = wallBuffer + baseFloorPaddingX + baseFloorX - baseFloorX / 2 + 1;
                        x < wallBuffer + xSize;
                        x++) {
                    layout[y][x] = LevelElement.FLOOR;
                }
            }
        }
        if (lowerLeft) {
            System.out.println("LOWER LEFT");
            for (int y = wallBuffer; y < wallBuffer + baseFloorPaddingY + baseFloorY / 2 - 1; y++) {
                for (int x = wallBuffer;
                        x < wallBuffer + baseFloorPaddingX + baseFloorX / 2 - 1;
                        x++) {
                    layout[y][x] = LevelElement.FLOOR;
                }
            }
        }
        if (lowerRight) {
            System.out.println("LOWER RIGHT");
            for (int y = wallBuffer; y < wallBuffer + baseFloorPaddingY + baseFloorY / 2 - 1; y++) {
                for (int x = wallBuffer + baseFloorPaddingX + baseFloorX - baseFloorX / 2 + 1;
                        x < wallBuffer + xSize;
                        x++) {
                    layout[y][x] = LevelElement.FLOOR;
                }
            }
        }

        // place Walls and Holes
        for (int y = 1; y < layout.length - 1; y++) {
            for (int x = 1; x < layout[0].length - 1; x++) {
                if (layout[y][x] == LevelElement.SKIP && neighborsFloor(layout, y, x))
                    layout[y][x] = LevelElement.WALL;
            }
        }

        // add random Holes
        final float PROBABILITY_FOR_HOLE = 0.02f;
        for (int y = wallBuffer; y < layout.length - wallBuffer; y++) {
            for (int x = wallBuffer; x < layout[0].length - wallBuffer; x++) {
                if (layout[y][x] == LevelElement.FLOOR && random.nextFloat() < PROBABILITY_FOR_HOLE)
                    // TODO change to HOLE
                    layout[y][x] = LevelElement.SKIP;
            }
        }

        // add supports for large rooms
        if (baseFloorX >= 15 && baseFloorY >= 15) {
            System.out.println("baseFloor: x=" + baseFloorX + ", y=" + baseFloorY);
            // lower left support
            for (int y = wallBuffer + baseFloorPaddingY + baseFloorY / 5;
                    y < wallBuffer + baseFloorPaddingY + baseFloorY / 5 + 3;
                    y++) {
                for (int x = wallBuffer + baseFloorPaddingX + baseFloorX / 5;
                        x < wallBuffer + baseFloorPaddingX + baseFloorX / 5 + 3;
                        x++) {
                    if (y == wallBuffer + baseFloorPaddingY + baseFloorY / 5 + 1
                            && x == wallBuffer + baseFloorPaddingX + baseFloorX / 5 + 1) {
                        layout[y][x] = LevelElement.SKIP;
                    } else {
                        layout[y][x] = LevelElement.WALL;
                    }
                }
            }

            // lower right support
            for (int y = wallBuffer + baseFloorPaddingY + baseFloorY / 5;
                    y < wallBuffer + baseFloorPaddingY + baseFloorY / 5 + 3;
                    y++) {
                for (int x = wallBuffer + baseFloorPaddingX + baseFloorX - baseFloorX / 5 - 3;
                        x < wallBuffer + baseFloorPaddingX + baseFloorX - baseFloorX / 5;
                        x++) {
                    if (y == wallBuffer + baseFloorPaddingY + baseFloorY / 5 + 1
                            && x
                                    == wallBuffer
                                            + baseFloorPaddingX
                                            + baseFloorX
                                            - baseFloorX / 5
                                            - 2) {
                        layout[y][x] = LevelElement.SKIP;
                    } else {
                        layout[y][x] = LevelElement.WALL;
                    }
                }
            }

            // upper left support
            for (int y = wallBuffer + baseFloorPaddingY + baseFloorY - baseFloorY / 5 - 3;
                    y < wallBuffer + baseFloorPaddingY + baseFloorY - baseFloorY / 5;
                    y++) {
                for (int x = wallBuffer + baseFloorPaddingX + baseFloorX / 5;
                        x < wallBuffer + baseFloorPaddingX + baseFloorX / 5 + 3;
                        x++) {
                    if (y == wallBuffer + baseFloorPaddingY + baseFloorY - baseFloorY / 5 - 2
                            && x == wallBuffer + baseFloorPaddingX + baseFloorX / 5 + 1) {
                        layout[y][x] = LevelElement.SKIP;
                    } else {
                        layout[y][x] = LevelElement.WALL;
                    }
                }
            }

            // upper right support
            for (int y = wallBuffer + baseFloorPaddingY + baseFloorY - baseFloorY / 5 - 3;
                    y < wallBuffer + baseFloorPaddingY + baseFloorY - baseFloorY / 5;
                    y++) {
                for (int x = wallBuffer + baseFloorPaddingX + baseFloorX - baseFloorX / 5 - 3;
                        x < wallBuffer + baseFloorPaddingX + baseFloorX - baseFloorX / 5;
                        x++) {
                    if (y == wallBuffer + baseFloorPaddingY + baseFloorY - baseFloorY / 5 - 2
                            && x
                                    == wallBuffer
                                            + baseFloorPaddingX
                                            + baseFloorX
                                            - baseFloorX / 5
                                            - 2) {
                        layout[y][x] = LevelElement.SKIP;
                    } else {
                        layout[y][x] = LevelElement.WALL;
                    }
                }
            }
        }

        printLayout(layout, size);
        return layout;
    }

    private boolean neighborsFloor(LevelElement[][] layout, int y, int x) {
        int floorNeighbors = 0;
        if (layout[y + 1][x - 1] == LevelElement.FLOOR) {
            floorNeighbors++;
        }
        if (layout[y + 1][x] == LevelElement.FLOOR) {
            floorNeighbors++;
        }
        if (layout[y + 1][x + 1] == LevelElement.FLOOR) {
            floorNeighbors++;
        }
        if (layout[y][x - 1] == LevelElement.FLOOR) {
            floorNeighbors++;
        }
        if (layout[y][x + 1] == LevelElement.FLOOR) {
            floorNeighbors++;
        }
        if (layout[y - 1][x - 1] == LevelElement.FLOOR) {
            floorNeighbors++;
        }
        if (layout[y - 1][x] == LevelElement.FLOOR) {
            floorNeighbors++;
        }
        if (layout[y - 1][x + 1] == LevelElement.FLOOR) {
            floorNeighbors++;
        }
        return floorNeighbors > 0;
    }

    private void printLayout(LevelElement[][] layout, LevelSize size) {
        System.out.println("LevelSize: " + size.name());
        System.out.println("xSize: " + layout[0].length);
        System.out.println("ySize: " + layout.length);
        for (int y = layout.length - 1; y >= 0; y--) {
            for (int x = 0; x < layout[0].length; x++) {
                switch (layout[y][x]) {
                    case SKIP -> System.out.print(". ");
                    case FLOOR -> System.out.print("# ");
                    case WALL -> System.out.print("W ");
                    case EXIT -> System.out.print("E ");
                }
            }
            System.out.println();
        }
    }
}
