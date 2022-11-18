package character.objects;

import minimap.IMinimap;
import room.Room;

/**
 * @author Lena Golin
 */
public class Letter implements Item {

    private char value;

    private IMinimap map;

    /**
     * Creates a Letter object that will be drawn on the minimap if collected
     *
     * @param value Character value that will be drawn on the map
     * @param map The minimap to draw on
     */
    private Room r;

    public Letter(char value, IMinimap map, Room r) {
        this.value = value;
        this.map = map;
        this.r = r;
    }

    @Override
    public void collect() {
        map.drawOnMap(value, r);
    }
}
