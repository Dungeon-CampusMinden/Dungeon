package cybersecurity;

import core.Game;
import core.level.DungeonLevel;
import core.level.Tile;
import core.level.elements.tile.DoorTile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;

import java.util.Map;

public class Level1 extends DungeonLevel {
    /**
     * Creates a new Demo Level.
     *
     * @param layout The layout of the level.
     * @param designLabel The design label of the level.
     * @param namedPoints The custom points of the level.
     */
    public Level1(LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
        super(layout, designLabel, namedPoints, "Cyber");
    }

    protected void onFirstTick() {
        DoorTile door = (DoorTile)(Game.tileAt(getPoint("door")).get());
        door.close();

    }
}
