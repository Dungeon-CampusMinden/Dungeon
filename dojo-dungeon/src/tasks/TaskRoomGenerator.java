package tasks;

import contrib.level.generator.graphBased.RoomGenerator;
import contrib.level.generator.graphBased.levelGraph.LevelNode;
import core.Entity;
import core.Game;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class TaskRoomGenerator {
  private RoomGenerator gen;
  private LevelNode room;
  private LevelNode nextNeighbour;
  private List<Task> roomTasks = new ArrayList<>();

  public TaskRoomGenerator(RoomGenerator gen, LevelNode room, LevelNode nextNeighbour) {
    this.gen = gen;
    this.room = room;
    this.nextNeighbour = nextNeighbour;
  }

  public void addTask(Task task) {
    roomTasks.add(task);
  }

  public Optional<Task> getNextUncompletedTask() {
    return roomTasks.stream().filter(t -> !t.isCompleted()).findFirst();
  }

  public boolean areAllTasksCompleted() {
    return roomTasks.stream().allMatch(Task::isCompleted);
  }

  public void addRoomEntities(Set<Entity> roomEntities) {
    // add the entities as payload to the LevelNode
    room.entities(roomEntities);

    // this will add the entities (in the node payload) to the game, at the moment the level get
    // loaded for the first time
    room.level().onFirstLoad(() -> room.entities().forEach(Game::add));
  }

  public RoomGenerator getGen() {
    return gen;
  }

  public LevelNode getRoom() {
    return room;
  }

  public LevelNode getNextNeighbour() {
    return nextNeighbour;
  }

  public abstract void generateRoom() throws IOException;
}
