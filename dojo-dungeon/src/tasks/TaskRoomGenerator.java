package tasks;

import contrib.level.generator.graphBased.RoomGenerator;
import contrib.level.generator.graphBased.levelGraph.LevelNode;
import core.Entity;
import core.Game;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface TaskRoomGenerator {
  List<Task> roomTasks = new ArrayList<>();

  default void addTask(Task task) {
    roomTasks.add(task);
  }

  default void addRoomEntities(LevelNode room, Set<Entity> roomEntities) {
    // add the entities as payload to the LevelNode
    room.entities(roomEntities);

    // this will add the entities (in the node payload) to the game, at the moment the level get
    // loaded for the first time
    room.level().onFirstLoad(() -> room.entities().forEach(Game::add));
  }

  void generateRoom(RoomGenerator gen, LevelNode room, LevelNode nextNeighbour) throws IOException;
}
