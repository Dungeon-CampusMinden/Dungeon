package level.level_2;

import contrib.level.generator.graphBased.RoomGenerator;
import core.level.TileLevel;
import core.level.utils.DesignLabel;
import core.level.utils.LevelSize;
import java.io.IOException;
import level.TaskRoomGenerator;
import level.room.DojoRoom;

public class Room_1_Generator extends TaskRoomGenerator {
  public Room_1_Generator(RoomGenerator gen, DojoRoom room, DojoRoom nextNeighbour) {
    super(gen, room, nextNeighbour);
  }

  @Override
  public void generateRoom() throws IOException {
    // TODO: ...
    // generate the room
    getRoom()
        .level(
            new TileLevel(
                getGen().layout(LevelSize.MEDIUM, getRoom().neighbours()), DesignLabel.ICE));
  }
}
