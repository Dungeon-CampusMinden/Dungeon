package roomlevel;

import java.util.LinkedHashSet;

public interface IRoom {
    LinkedHashSet<DoorTile> getDoors();

    void removeExit();
}
