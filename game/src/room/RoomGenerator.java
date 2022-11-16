package room;

import static level.elements.ILevel.RANDOM;

import java.util.ArrayList;
import java.util.Random;
import level.tools.Coordinate;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import level.tools.LevelSize;

public class RoomGenerator {

    public static final float SYMMETRICAL = 0.5f;
    public static final float EXTEND_TO_SIDES = 0.5f;
    public static final float PROBABILITY_SIDE = 0.75f;
    public static final float PROBABILITY_CORNER = 0.75f;
    private static final float PROBABILITY_HOLE = 0.02f;

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

    public IRoom getLevel(DesignLabel designLabel, LevelSize size) {
        return new Room(getLayout(size), designLabel);
    }

    public LevelElement[][] getLayout(LevelSize size) {
        return generateRoom(size, RANDOM.nextLong());
    }

    private LevelElement[][] generateRoom(LevelSize size, long seed) {
        // Initialize random number generator with seed
        Random random = new Random(seed);

        // Define max room size
        int xSize;
        int ySize;

        switch (size) {
            case SMALL -> {
                xSize = random.nextInt(SMALL_MIN_X_SIZE, SMALL_MAX_X_SIZE + 1);
                ySize = random.nextInt(SMALL_MIN_Y_SIZE, SMALL_MAX_Y_SIZE + 1);
            }
            case LARGE -> {
                xSize = random.nextInt(BIG_MIN_X_SIZE, BIG_MAX_X_SIZE + 1);
                ySize = random.nextInt(BIG_MIN_Y_SIZE, BIG_MAX_Y_SIZE + 1);
            }
            default -> {
                xSize = random.nextInt(MEDIUM_MIN_X_SIZE, MEDIUM_MAX_X_SIZE + 1);
                ySize = random.nextInt(MEDIUM_MIN_Y_SIZE, MEDIUM_MAX_Y_SIZE + 1);
            }
        }

        // Initialize layout with additional buffer for wall and skip layer
        final int WALL_BUFFER = 2;
        LevelElement[][] layout =
                new LevelElement[ySize + WALL_BUFFER * 2][xSize + WALL_BUFFER * 2];

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
                BASE_FLOOR_X_SIZE = new MinMaxValue(BIG_MIN_X_SIZE - 10, xSize - 4);
                BASE_FLOOR_Y_SIZE = new MinMaxValue(BIG_MIN_Y_SIZE - 10, ySize - 4);
            }
            default -> {
                BASE_FLOOR_X_SIZE = new MinMaxValue(MEDIUM_MIN_X_SIZE - 8, xSize - 3);
                BASE_FLOOR_Y_SIZE = new MinMaxValue(MEDIUM_MIN_Y_SIZE - 8, ySize - 3);
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

        for (int y = WALL_BUFFER + baseFloorPaddingY;
                y < layout.length - WALL_BUFFER - baseFloorPaddingY;
                y++) {
            for (int x = WALL_BUFFER + baseFloorPaddingX;
                    x < layout[0].length - WALL_BUFFER - baseFloorPaddingX;
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
        if (random.nextFloat() < SYMMETRICAL) {
            symmetrical = true;
        }
        // Small rooms cannot extend to the corners
        // Medium and Big Rooms can extend to corner or sides
        if (size == LevelSize.SMALL || random.nextFloat() < EXTEND_TO_SIDES) {
            // extend to sides
            if (random.nextFloat() < PROBABILITY_SIDE) {
                up = true;
            }
            if (random.nextFloat() < PROBABILITY_SIDE) {
                down = true;
            }
            if (random.nextFloat() < PROBABILITY_SIDE) {
                left = true;
            }
            if (random.nextFloat() < PROBABILITY_SIDE) {
                right = true;
            }
        } else {
            // extend to corners
            if (random.nextFloat() < PROBABILITY_CORNER) {
                upperLeft = true;
            }
            if (random.nextFloat() < PROBABILITY_CORNER) {
                upperRight = true;
            }
            if (random.nextFloat() < PROBABILITY_CORNER) {
                lowerLeft = true;
            }
            if (random.nextFloat() < PROBABILITY_CORNER) {
                lowerRight = true;
            }
        }
        if (up) {
            System.out.println("UP");
            for (int y = WALL_BUFFER + baseFloorPaddingY + baseFloorY;
                    y < WALL_BUFFER + ySize;
                    y++) {
                for (int x = WALL_BUFFER + baseFloorPaddingX;
                        x < WALL_BUFFER + baseFloorPaddingX + baseFloorX;
                        x++) {
                    layout[y][x] = LevelElement.FLOOR;
                }
            }
        }
        if (down) {
            System.out.println("DOWN");
            for (int y = WALL_BUFFER; y < WALL_BUFFER + baseFloorPaddingY; y++) {
                for (int x = WALL_BUFFER + baseFloorPaddingX;
                        x < WALL_BUFFER + baseFloorPaddingX + baseFloorX;
                        x++) {
                    layout[y][x] = LevelElement.FLOOR;
                }
            }
        }
        if (left) {
            System.out.println("LEFT");
            for (int y = WALL_BUFFER + baseFloorPaddingY;
                    y < WALL_BUFFER + baseFloorPaddingY + baseFloorY;
                    y++) {
                for (int x = WALL_BUFFER; x < WALL_BUFFER + baseFloorPaddingX; x++) {
                    layout[y][x] = LevelElement.FLOOR;
                }
            }
        }
        if (right) {
            System.out.println("RIGHT");
            for (int y = WALL_BUFFER + baseFloorPaddingY;
                    y < WALL_BUFFER + baseFloorPaddingY + baseFloorY;
                    y++) {
                for (int x = WALL_BUFFER + baseFloorPaddingX + baseFloorX;
                        x < WALL_BUFFER + xSize;
                        x++) {
                    layout[y][x] = LevelElement.FLOOR;
                }
            }
        }
        if (upperLeft) {
            System.out.println("UPPER LEFT");
            for (int y = WALL_BUFFER + baseFloorPaddingY + baseFloorY - baseFloorY / 2 + 1;
                    y < WALL_BUFFER + ySize;
                    y++) {
                for (int x = WALL_BUFFER;
                        x < WALL_BUFFER + baseFloorPaddingX + baseFloorX / 2 - 1;
                        x++) {
                    layout[y][x] = LevelElement.FLOOR;
                }
            }
        }
        if (upperRight) {
            System.out.println("UPPER RIGHT");
            for (int y = WALL_BUFFER + baseFloorPaddingY + baseFloorY - baseFloorY / 2 + 1;
                    y < WALL_BUFFER + ySize;
                    y++) {
                for (int x = WALL_BUFFER + baseFloorPaddingX + baseFloorX - baseFloorX / 2 + 1;
                        x < WALL_BUFFER + xSize;
                        x++) {
                    layout[y][x] = LevelElement.FLOOR;
                }
            }
        }
        if (lowerLeft) {
            System.out.println("LOWER LEFT");
            for (int y = WALL_BUFFER;
                    y < WALL_BUFFER + baseFloorPaddingY + baseFloorY / 2 - 1;
                    y++) {
                for (int x = WALL_BUFFER;
                        x < WALL_BUFFER + baseFloorPaddingX + baseFloorX / 2 - 1;
                        x++) {
                    layout[y][x] = LevelElement.FLOOR;
                }
            }
        }
        if (lowerRight) {
            System.out.println("LOWER RIGHT");
            for (int y = WALL_BUFFER;
                    y < WALL_BUFFER + baseFloorPaddingY + baseFloorY / 2 - 1;
                    y++) {
                for (int x = WALL_BUFFER + baseFloorPaddingX + baseFloorX - baseFloorX / 2 + 1;
                        x < WALL_BUFFER + xSize;
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
        for (int y = WALL_BUFFER; y < layout.length - WALL_BUFFER; y++) {
            for (int x = WALL_BUFFER; x < layout[0].length - WALL_BUFFER; x++) {
                if (layout[y][x] == LevelElement.FLOOR && random.nextFloat() < PROBABILITY_HOLE)
                    layout[y][x] = LevelElement.HOLE;
            }
        }

        // add supports for large rooms
        if (baseFloorX >= 15 && baseFloorY >= 15) {

            int leftSupportLeftSide = WALL_BUFFER + baseFloorPaddingX + baseFloorX / 5;
            int leftSupportRightSide = WALL_BUFFER + baseFloorPaddingX + baseFloorX / 5 + 2;
            int bottomSupportBottomSide = WALL_BUFFER + baseFloorPaddingY + baseFloorY / 5;
            int bottomSupportUpperSide = WALL_BUFFER + baseFloorPaddingY + baseFloorY / 5 + 2;
            int rightSupportLeftSide =
                    WALL_BUFFER + baseFloorPaddingX + baseFloorX - baseFloorX / 5 - 3;
            int rightSupportRightSide =
                    WALL_BUFFER + baseFloorPaddingX + baseFloorX - baseFloorX / 5 - 1;
            int upperSupportBottomSide =
                    WALL_BUFFER + baseFloorPaddingY + baseFloorY - baseFloorY / 5 - 3;
            int upperSupportUpperSide =
                    WALL_BUFFER + baseFloorPaddingY + baseFloorY - baseFloorY / 5 - 1;
            // lower left support
            for (int y = bottomSupportBottomSide; y <= bottomSupportUpperSide; y++) {
                for (int x = leftSupportLeftSide; x <= leftSupportRightSide; x++) {
                    if (y == bottomSupportBottomSide + 1 && x == leftSupportLeftSide + 1) {
                        layout[y][x] = LevelElement.SKIP;
                    } else {
                        layout[y][x] = LevelElement.WALL;
                    }
                }
            }

            // lower right support
            for (int y = bottomSupportBottomSide; y <= bottomSupportUpperSide; y++) {
                for (int x = rightSupportLeftSide; x <= rightSupportRightSide; x++) {
                    if (y == bottomSupportBottomSide + 1 && x == rightSupportLeftSide + 1) {
                        layout[y][x] = LevelElement.SKIP;
                    } else {
                        layout[y][x] = LevelElement.WALL;
                    }
                }
            }

            // upper left support
            for (int y = upperSupportBottomSide; y <= upperSupportUpperSide; y++) {
                for (int x = leftSupportLeftSide; x <= leftSupportRightSide; x++) {
                    if (y == upperSupportBottomSide + 1 && x == leftSupportLeftSide + 1) {
                        layout[y][x] = LevelElement.SKIP;
                    } else {
                        layout[y][x] = LevelElement.WALL;
                    }
                }
            }

            // upper right support
            for (int y = upperSupportBottomSide; y <= upperSupportUpperSide; y++) {
                for (int x = rightSupportLeftSide; x <= rightSupportRightSide; x++) {
                    if (y == upperSupportBottomSide + 1 && x == rightSupportLeftSide + 1) {
                        layout[y][x] = LevelElement.SKIP;
                    } else {
                        layout[y][x] = LevelElement.WALL;
                    }
                }
            }
        }

        // Add doors
        // TODO take information from doors parameter
        boolean upperDoor = random.nextBoolean();
        boolean bottomDoor = random.nextBoolean();
        boolean leftDoor = random.nextBoolean();
        boolean rightDoor = random.nextBoolean();

        if (upperDoor) {
            ArrayList<Coordinate> possibleDoorCoordinates = new ArrayList<>();
            for (int y = WALL_BUFFER + ySize; y > WALL_BUFFER; y--) {
                for (int x = WALL_BUFFER; x < WALL_BUFFER + xSize; x++) {
                    // only mark walls that are not next to a corner
                    if (layout[y][x] == LevelElement.WALL
                            && layout[y - 1][x - 1] != LevelElement.WALL
                            && layout[y - 1][x + 1] != LevelElement.WALL
                            && layout[y][x - 1] == LevelElement.WALL
                            && layout[y][x + 1] == LevelElement.WALL) {
                        possibleDoorCoordinates.add(new Coordinate(x, y));
                    }
                }
                if (!possibleDoorCoordinates.isEmpty()) {
                    break;
                }
            }
            // TODO throw exception if possibleDoorCoordinates.size() == 0
            int doorIndex = random.nextInt(possibleDoorCoordinates.size());
            Coordinate doorCoordinate = possibleDoorCoordinates.get(doorIndex);
            // TODO change to DOOR
            // layout[doorCoordinate.y][doorCoordinate.x] = LevelElement.DOOR;
            layout[doorCoordinate.y][doorCoordinate.x] = LevelElement.EXIT;
        }
        if (bottomDoor) {
            ArrayList<Coordinate> possibleDoorCoordinates = new ArrayList<>();
            for (int y = WALL_BUFFER - 1; y < WALL_BUFFER + ySize; y++) {
                for (int x = WALL_BUFFER; x < WALL_BUFFER + xSize; x++) {
                    // only mark walls that are not next to a corner
                    if (layout[y][x] == LevelElement.WALL
                            && layout[y + 1][x - 1] != LevelElement.WALL
                            && layout[y + 1][x + 1] != LevelElement.WALL
                            && layout[y][x - 1] == LevelElement.WALL
                            && layout[y][x + 1] == LevelElement.WALL) {
                        possibleDoorCoordinates.add(new Coordinate(x, y));
                    }
                }
                if (!possibleDoorCoordinates.isEmpty()) {
                    break;
                }
            }
            // TODO throw exception if possibleDoorCoordinates.size() == 0
            int doorIndex = random.nextInt(possibleDoorCoordinates.size());
            Coordinate doorCoordinate = possibleDoorCoordinates.get(doorIndex);
            // TODO change to DOOR
            // layout[doorCoordinate.y][doorCoordinate.x] = LevelElement.DOOR;
            layout[doorCoordinate.y][doorCoordinate.x] = LevelElement.EXIT;
        }
        if (leftDoor) {
            ArrayList<Coordinate> possibleDoorCoordinates = new ArrayList<>();
            for (int x = WALL_BUFFER - 1; x < WALL_BUFFER + xSize; x++) {
                for (int y = WALL_BUFFER; y < WALL_BUFFER + ySize; y++) {
                    // only mark walls that are not next to a corner
                    if (layout[y][x] == LevelElement.WALL
                            && layout[y - 1][x + 1] != LevelElement.WALL
                            && layout[y + 1][x + 1] != LevelElement.WALL
                            && layout[y - 1][x] == LevelElement.WALL
                            && layout[y + 1][x] == LevelElement.WALL) {
                        possibleDoorCoordinates.add(new Coordinate(x, y));
                    }
                }
                if (!possibleDoorCoordinates.isEmpty()) {
                    break;
                }
            }
            // TODO throw exception if possibleDoorCoordinates.size() == 0
            int doorIndex = random.nextInt(possibleDoorCoordinates.size());
            Coordinate doorCoordinate = possibleDoorCoordinates.get(doorIndex);
            // TODO change to DOOR
            // layout[doorCoordinate.y][doorCoordinate.x] = LevelElement.DOOR;
            layout[doorCoordinate.y][doorCoordinate.x] = LevelElement.EXIT;
        }
        if (rightDoor) {
            ArrayList<Coordinate> possibleDoorCoordinates = new ArrayList<>();
            for (int x = WALL_BUFFER + xSize; x > WALL_BUFFER; x--) {
                for (int y = WALL_BUFFER; y < WALL_BUFFER + ySize; y++) {
                    // only mark walls that are not next to a corner
                    if (layout[y][x] == LevelElement.WALL
                            && layout[y - 1][x - 1] != LevelElement.WALL
                            && layout[y + 1][x - 1] != LevelElement.WALL
                            && layout[y - 1][x] == LevelElement.WALL
                            && layout[y + 1][x] == LevelElement.WALL) {
                        possibleDoorCoordinates.add(new Coordinate(x, y));
                    }
                }
                if (!possibleDoorCoordinates.isEmpty()) {
                    break;
                }
            }
            // TODO throw exception if possibleDoorCoordinates.size() == 0
            int doorIndex = random.nextInt(possibleDoorCoordinates.size());
            Coordinate doorCoordinate = possibleDoorCoordinates.get(doorIndex);
            // TODO change to DOOR
            // layout[doorCoordinate.y][doorCoordinate.x] = LevelElement.DOOR;
            layout[doorCoordinate.y][doorCoordinate.x] = LevelElement.EXIT;
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
                    case SKIP -> System.out.print("  ");
                    case FLOOR -> System.out.print(". ");
                    case WALL -> System.out.print("W ");
                    case EXIT -> System.out.print("E ");
                }
            }
            System.out.println();
        }
    }
}
