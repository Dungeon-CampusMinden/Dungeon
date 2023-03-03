package level;

import static org.junit.Assert.assertEquals;

import level.tools.Coordinate;
import level.tools.DesignLabel;
import level.tools.LevelElement;
import level.tools.TileTextureFactory;
import org.junit.Before;
import org.junit.Test;

public class TileTextureFactoryTest {

    private String targetTexture;
    private DesignLabel design;
    private LevelElement[][] layout;

    @Before
    public void setup() {
        design = DesignLabel.DEFAULT;
        targetTexture = "dungeon/default/";
        layout = new LevelElement[10][11];

        // Test Level Layout
        // S S S S S S S S S S S
        // S W W W W W W W W W S
        // S W F E F F H H F W S
        // S W F F F F F H F W S
        // S W F W W F F F F W S
        // S W W W W F W W F W S
        // S S S W F F W W F W S
        // S S S W F F F W W W S
        // S S S W W W W W S S S
        // S S S S S S S S S S S

        // skips
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 11; x++) {
                layout[y][x] = LevelElement.SKIP;
            }
        }

        // floors
        for (int y = 5; y < 8; y++) {
            for (int x = 2; x < 9; x++) {
                layout[y][x] = LevelElement.FLOOR;
            }
        }
        for (int y = 2; y < 5; y++) {
            for (int x = 4; x < 9; x++) {
                layout[y][x] = LevelElement.FLOOR;
            }
        }

        // outer walls
        for (int y = 4; y < 9; y++) {
            layout[y][1] = LevelElement.WALL;
        }
        for (int x = 2; x < 10; x++) {
            layout[8][x] = LevelElement.WALL;
        }
        for (int y = 2; y < 9; y++) {
            layout[y][9] = LevelElement.WALL;
        }
        for (int x = 7; x < 9; x++) {
            layout[2][x] = LevelElement.WALL;
        }
        for (int x = 3; x < 8; x++) {
            layout[1][x] = LevelElement.WALL;
        }
        for (int y = 2; y < 5; y++) {
            layout[y][3] = LevelElement.WALL;
        }
        layout[4][2] = LevelElement.WALL;

        // inner walls
        for (int y = 4; y < 6; y++) {
            for (int x = 3; x < 5; x++) {
                layout[y][x] = LevelElement.WALL;
            }
        }
        for (int y = 3; y < 5; y++) {
            for (int x = 6; x < 8; x++) {
                layout[y][x] = LevelElement.WALL;
            }
        }

        // exit
        layout[7][3] = LevelElement.EXIT;

        // holes
        layout[6][7] = LevelElement.HOLE;
        layout[7][6] = LevelElement.HOLE;
        layout[7][7] = LevelElement.HOLE;

        // doors
        layout[1][5] = LevelElement.DOOR;
        layout[6][1] = LevelElement.DOOR;
        layout[5][9] = LevelElement.DOOR;
        layout[8][4] = LevelElement.DOOR;
    }

    /** Level Element SKIP should get "empty" texture. */
    @Test
    public void test_findTexturePath_skip() {
        targetTexture += "floor/empty.png";
        Coordinate coordinate = new Coordinate(2, 3);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }

    /** Level Element FLOOR should get "floor_1" texture. */
    @Test
    public void test_findTexturePath_floor() {
        targetTexture += "floor/floor_1.png";
        Coordinate coordinate = new Coordinate(5, 4);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }

    /** Level Element EXIT should get "floor_ladder" texture. */
    @Test
    public void test_findTexturePath_exit() {
        targetTexture += "floor/floor_ladder.png";
        Coordinate coordinate = new Coordinate(3, 7);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }

    /** Level Element HOLE should get "floor_hole" texture if above is not another HOLE. */
    @Test
    public void test_findTexturePath_hole() {
        targetTexture += "floor/floor_hole.png";
        Coordinate coordinate = new Coordinate(7, 7);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
        coordinate = new Coordinate(6, 7);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }

    /** Level Element HOLE should get "floor_hole1" texture if above is another HOLE. */
    @Test
    public void test_findTexturePath_holeBelowHole() {
        targetTexture += "floor/floor_hole1.png";
        Coordinate coordinate = new Coordinate(7, 6);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }

    /**
     * Level Element WALL should get "wall_right" texture if left is FLOOR/HOLE, and it is not an
     * inner corner or cross.
     */
    @Test
    public void test_findTexturePath_rightWall() {
        targetTexture += "wall/wall_right.png";
        Coordinate coordinate = new Coordinate(9, 6);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }

    /**
     * Level Element WALL should get "wall_left" texture if right is FLOOR/HOLE, and it is not an
     * inner corner or cross.
     */
    @Test
    public void test_findTexturePath_leftWall() {
        targetTexture += "wall/wall_left.png";
        Coordinate coordinate = new Coordinate(3, 3);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }

    /**
     * Level Element WALL should get "wall_bottom" texture if above is FLOOR/HOLE, and it is not an
     * inner corner or cross.
     */
    @Test
    public void test_findTexturePath_bottomWall() {
        targetTexture += "wall/wall_bottom.png";
        Coordinate coordinate = new Coordinate(2, 4);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
        coordinate = new Coordinate(6, 1);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
        coordinate = new Coordinate(8, 2);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }

    /**
     * Level Element WALL should get "wall_top" texture if below is FLOOR/HOLE, and it is not an
     * inner corner or cross.
     */
    @Test
    public void test_findTexturePath_topWall() {
        targetTexture += "wall/wall_top.png";
        Coordinate coordinate = new Coordinate(3, 8);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
        coordinate = new Coordinate(5, 8);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
        coordinate = new Coordinate(7, 8);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }

    /** Level Element DOOR should get "right" texture if left is accessible. */
    @Test
    public void test_findTexturePath_rightDoor() {
        targetTexture += "door/right.png";
        Coordinate coordinate = new Coordinate(9, 5);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }

    /** Level Element DOOR should get "left" texture if right is accessible. */
    @Test
    public void test_findTexturePath_leftDoor() {
        targetTexture += "door/left.png";
        Coordinate coordinate = new Coordinate(1, 6);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }

    /** Level Element DOOR should get "bottom" texture if above is accessible. */
    @Test
    public void test_findTexturePath_bottomDoor() {
        targetTexture += "door/bottom.png";
        Coordinate coordinate = new Coordinate(5, 1);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }

    /** Level Element DOOR should get "top" texture if below is accessible. */
    @Test
    public void test_findTexturePath_topDoor() {
        targetTexture += "door/top.png";
        Coordinate coordinate = new Coordinate(4, 8);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }

    /**
     * Level Element WALL should get "wall_outer_corner_upper_left" texture if bottom right is
     * FLOOR, below is WALL and right is WALL.
     */
    @Test
    public void test_findTexturePath_upperLeftOuterCorner() {
        targetTexture += "wall/wall_outer_corner_upper_left.png";
        Coordinate coordinate = new Coordinate(1, 8);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }

    /**
     * Level Element WALL should get "wall_outer_corner_upper_right" texture if bottom left is
     * FLOOR, below is WALL and left is WALL.
     */
    @Test
    public void test_findTexturePath_upperRightOuterCorner() {
        targetTexture += "wall/wall_outer_corner_upper_right.png";
        Coordinate coordinate = new Coordinate(9, 8);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }

    /**
     * Level Element WALL should get "wall_outer_corner_bottom_right" texture if upper left is
     * FLOOR, above is WALL and left is WALL.
     */
    @Test
    public void test_findTexturePath_bottomRightOuterCorner() {
        targetTexture += "wall/wall_outer_corner_bottom_right.png";
        Coordinate coordinate = new Coordinate(9, 2);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
        coordinate = new Coordinate(7, 1);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }

    /**
     * Level Element WALL should get "wall_outer_corner_bottom_left" texture if upper right is
     * FLOOR, above is WALL and right is WALL.
     */
    @Test
    public void test_findTexturePath_bottomLeftOuterCorner() {
        targetTexture += "wall/wall_outer_corner_bottom_left.png";
        Coordinate coordinate = new Coordinate(3, 1);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
        coordinate = new Coordinate(1, 4);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }

    /**
     * Level Element WALL should get "wall_inner_corner_upper_left" texture if below is WALL, right
     * is WALL and inner corner conditions are met.
     */
    @Test
    public void test_findTexturePath_upperLeftInnerCorner() {
        targetTexture += "wall/wall_inner_corner_upper_left.png";
        Coordinate coordinate = new Coordinate(3, 5);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
        coordinate = new Coordinate(6, 4);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
        coordinate = new Coordinate(7, 2);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }

    /**
     * Level Element WALL should get "wall_inner_corner_upper_right" texture if below is WALL, left
     * is WALL and inner corner conditions are met.
     */
    @Test
    public void test_findTexturePath_upperRightInnerCorner() {
        targetTexture += "wall/wall_inner_corner_upper_right.png";
        Coordinate coordinate = new Coordinate(4, 5);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
        coordinate = new Coordinate(7, 4);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }

    /**
     * Level Element WALL should get "wall_inner_corner_bottom_left" texture if above is WALL, right
     * is WALL and inner corner conditions are met.
     */
    @Test
    public void test_findTexturePath_bottomLeftInnerCorner() {
        targetTexture += "wall/wall_inner_corner_bottom_left.png";
        Coordinate coordinate = new Coordinate(6, 3);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }

    /**
     * Level Element WALL should get "wall_inner_corner_bottom_right" texture if above is WALL, left
     * is WALL and inner corner conditions are met.
     */
    @Test
    public void test_findTexturePath_bottomRightInnerCorner() {
        targetTexture += "wall/wall_inner_corner_bottom_right.png";
        Coordinate coordinate = new Coordinate(4, 4);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
        coordinate = new Coordinate(7, 3);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }

    /**
     * Level Element WALL should get "wall_cross_upper_left_bottom_right" texture if above is WALL,
     * left is WALL, below is WALL, right is WALL, upper left is FLOOR and bottom right is FLOOR.
     */
    @Test
    public void test_findTexturePath_upperLeftBottomRightCross() {
        targetTexture += "wall/wall_cross_upper_left_bottom_right.png";
        Coordinate coordinate = new Coordinate(3, 4);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        new TileTextureFactory.LevelPart(
                                layout[coordinate.y][coordinate.x], design, layout, coordinate)));
    }
}
