package level.elements;

import level.tools.LevelElement;
import level.tools.TileTextureFactory;

public interface ILevel extends ITileable {

    /** Mark a random tile as start */
    default void setRandomStart() {
        setStartTile(getRandomTile(LevelElement.FLOOR));
    }

    /**
     * Set the start tile.
     *
     * @param start The start tile.
     */
    void setStartTile(Tile start);

    /** Mark a random tile as end */
    default void setRandomEnd() {
        setEndTile(getRandomTile(LevelElement.FLOOR));
    }

    /**
     * Set the end tile.
     *
     * @param end The end tile.
     */
    void setEndTile(Tile end);

    /**
     * F=Floor, W=Wall, E=Exit, S=Skip/Blank
     *
     * @return The level layout in String format
     */
    default String printLevel() {
        StringBuilder output = new StringBuilder();
        for (int y = 0; y < getLayout().length; y++) {
            for (int x = 0; x < getLayout()[0].length; x++) {
                if (getLayout()[y][x].getLevelElement() == LevelElement.FLOOR) {
                    output.append("F");
                } else if (getLayout()[y][x].getLevelElement() == LevelElement.WALL) {
                    output.append("W");
                } else if (getLayout()[y][x].getLevelElement() == LevelElement.EXIT) {
                    output.append("E");
                } else {
                    output.append("S");
                }
            }
            output.append("\n");
        }
        return output.toString();
    }

    /**
     * Change the type of tile (including changing texture)
     *
     * @param tile The Tile you want to change
     * @param changeInto The LevelElement to change the Tile into.
     */
    default void changeTileElementType(Tile tile, LevelElement changeInto) {
        tile.setLevelElement(
                changeInto, TileTextureFactory.findTexturePath(tile, getLayout(), changeInto));
    }
}
