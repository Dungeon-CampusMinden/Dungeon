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
        targetTexture = "textures/dungeon/default/";
        layout = new LevelElement[10][11];

        // skips
        for (int y = 0; y < 10; y++) {
            for (int x = 0; x < 2; x++) {
                layout[y][x] = LevelElement.SKIP;
            }
        }

        // outer walls and inner floors
        for (int y = 0; y < 10; y++) {
            for (int x = 2; x < 11; x++) {
                if (y > 0 && y < 9 && x > 2 && x < 10) {
                    layout[y][x] = LevelElement.FLOOR;
                } else {
                    layout[y][x] = LevelElement.WALL;
                }
            }
        }

        // inner walls
        for (int y = 0; y < 10; y++) {
            layout[y][8] = LevelElement.WALL;
        }
        for (int x = 2; x < 9; x++) {
            layout[4][x] = LevelElement.WALL;
        }

        // exit
        layout[1][6] = LevelElement.EXIT;
        // in space WALL
        layout[2][0] = LevelElement.WALL;
    }

    @Test
    public void test_findTexturePath_skip() {
        targetTexture += "floor/empty.png";
        Coordinate coordinate = new Coordinate(0, 5);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        layout[coordinate.y][coordinate.x], design, layout, coordinate));
    }

    @Test
    public void test_findTexturePath_floor() {
        targetTexture += "floor/floor_1.png";
        Coordinate coordinate = new Coordinate(5, 2);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        layout[coordinate.y][coordinate.x], design, layout, coordinate));
    }

    @Test
    public void test_findTexturePath_exit() {
        targetTexture += "floor/floor_ladder.png";
        Coordinate coordinate = new Coordinate(6, 1);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        layout[coordinate.y][coordinate.x], design, layout, coordinate));
    }

    @Test
    public void test_findTexturePath_nonPlayableArea() {
        targetTexture += "floor/empty.png";
        Coordinate coordinate = new Coordinate(0, 2);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        layout[coordinate.y][coordinate.x], design, layout, coordinate));
    }

    @Test
    public void test_findTexturePath_rightWall() {
        targetTexture += "wall/right.png";
        Coordinate coordinate = new Coordinate(10, 2);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        layout[coordinate.y][coordinate.x], design, layout, coordinate));
    }

    @Test
    public void test_findTexturePath_leftWall() {
        targetTexture += "wall/left.png";
        Coordinate coordinate = new Coordinate(2, 2);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        layout[coordinate.y][coordinate.x], design, layout, coordinate));
    }

    @Test
    public void test_findTexturePath_sideWall() {
        targetTexture += "wall/side.png";
        Coordinate coordinate = new Coordinate(8, 2);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        layout[coordinate.y][coordinate.x], design, layout, coordinate));
    }

    @Test
    public void test_findTexturePath_bottomWall() {
        targetTexture += "wall/bottom.png";
        Coordinate coordinate = new Coordinate(4, 0);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        layout[coordinate.y][coordinate.x], design, layout, coordinate));
    }

    @Test
    public void test_findTexturePath_topWall() {
        targetTexture += "wall/top.png";
        Coordinate coordinate = new Coordinate(5, 9);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        layout[coordinate.y][coordinate.x], design, layout, coordinate));
    }

    @Test
    public void test_findTexturePath_bottomAndTopWall() {
        targetTexture += "wall/top_bottom.png";
        Coordinate coordinate = new Coordinate(4, 4);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        layout[coordinate.y][coordinate.x], design, layout, coordinate));
        coordinate = new Coordinate(3, 4);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        layout[coordinate.y][coordinate.x], design, layout, coordinate));
    }

    @Test
    public void test_findTexturePath_upperLeftCorner() {
        targetTexture += "wall/corner_upper_left.png";
        Coordinate coordinate = new Coordinate(2, 9);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        layout[coordinate.y][coordinate.x], design, layout, coordinate));
    }

    @Test
    public void test_findTexturePath_upperRightCorner() {
        targetTexture += "wall/corner_upper_right.png";
        Coordinate coordinate = new Coordinate(10, 9);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        layout[coordinate.y][coordinate.x], design, layout, coordinate));
    }

    @Test
    public void test_findTexturePath_bottomRightCorner() {
        targetTexture += "wall/corner_bottom_right.png";
        Coordinate coordinate = new Coordinate(10, 0);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        layout[coordinate.y][coordinate.x], design, layout, coordinate));
    }

    @Test
    public void test_findTexturePath_bottomLeftCorner() {
        targetTexture += "wall/corner_bottom_left.png";
        Coordinate coordinate = new Coordinate(2, 0);
        assertEquals(
                targetTexture,
                TileTextureFactory.findTexturePath(
                        layout[coordinate.y][coordinate.x], design, layout, coordinate));
    }
}
