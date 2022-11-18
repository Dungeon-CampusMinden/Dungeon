package minimap;

import room.Room;

public interface IMinimap {

    /**
     * Draws Letter on minimap
     *
     * @param c Letter that will be drawn onto the minimap
     */
    void drawOnMap(char c, Room r);

    void drawMap();
}
